package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.AuthResponse;
import com.chandravijay.banking.dto.ForgotPasswordRequest;
import com.chandravijay.banking.dto.LoginRequest;
import com.chandravijay.banking.dto.OtpResponse;
import com.chandravijay.banking.dto.RegisterRequest;
import com.chandravijay.banking.dto.ResetPasswordRequest;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.Role;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.DuplicateResourceException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.security.JwtUtil;
import com.chandravijay.banking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int RESET_OTP_VALIDITY_SECONDS = 300; // 5 minutes

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditLogHelperService auditLogHelper;
    private final NotificationHelperService notificationHelper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();

        userRepository.save(user);
        auditLogHelper.log(user.getUsername(), "REGISTER", "New account registered: " + user.getEmail());

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        // If the user already crossed the failure threshold on a previous attempt, lock now
        // (before even trying to authenticate) so CustomUserDetailsService reports it as locked.
        if (user != null && user.isLocked()) {
            auditLogHelper.log(request.getUsername(), "LOGIN_BLOCKED", "Attempted login on a locked account", true);
            throw new LockedException("Account is locked");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User authenticatedUser = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

            // Successful login: reset the failure counter and record the timestamp.
            authenticatedUser.setFailedLoginAttempts(0);
            authenticatedUser.setLastLoginAt(LocalDateTime.now());
            userRepository.save(authenticatedUser);

            auditLogHelper.log(request.getUsername(), "LOGIN_SUCCESS", "Successful login");

            String token = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .username(authenticatedUser.getUsername())
                    .role(authenticatedUser.getRole().name())
                    .build();

        } catch (BadCredentialsException ex) {
            if (user != null) {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                boolean nowLocked = attempts >= MAX_FAILED_ATTEMPTS;
                if (nowLocked) {
                    user.setLocked(true);
                }
                userRepository.save(user);

                auditLogHelper.log(request.getUsername(), "LOGIN_FAILED",
                        "Failed login attempt " + attempts + "/" + MAX_FAILED_ATTEMPTS
                                + (nowLocked ? " — account now locked" : ""),
                        nowLocked);
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that username"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setResetOtpCode(otp);
        user.setResetOtpExpiresAt(LocalDateTime.now().plusSeconds(RESET_OTP_VALIDITY_SECONDS));
        userRepository.save(user);

        notificationHelper.notify(user, NotificationType.SECURITY_ALERT,
                "A password reset was requested for your account. If this wasn't you, contact support immediately.");
        auditLogHelper.log(request.getUsername(), "PASSWORD_RESET_REQUESTED", "OTP generated", true);

        return OtpResponse.builder()
                .message("OTP generated. Use it within 5 minutes to set a new password.")
                .devOnlyOtp(otp)
                .expiresInSeconds(RESET_OTP_VALIDITY_SECONDS)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that username"));

        if (user.getResetOtpExpiresAt() == null || user.getResetOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccountOperationException("OTP has expired — request a new one");
        }
        if (user.getResetOtpCode() == null || !user.getResetOtpCode().equals(request.getOtp())) {
            throw new AccountOperationException("Incorrect OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetOtpCode(null);
        user.setResetOtpExpiresAt(null);
        // A successful reset also clears any lockout — the person just proved they own the account.
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        userRepository.save(user);

        notificationHelper.notify(user, NotificationType.SECURITY_ALERT, "Your password was successfully reset.");
        auditLogHelper.log(request.getUsername(), "PASSWORD_RESET_COMPLETED", "Password changed via forgot-password flow");
    }
}
