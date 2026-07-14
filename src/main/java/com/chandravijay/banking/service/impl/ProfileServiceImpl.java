package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.KycSubmitRequest;
import com.chandravijay.banking.dto.ProfileResponse;
import com.chandravijay.banking.dto.UpdateProfileRequest;
import com.chandravijay.banking.entity.KycStatus;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.DuplicateResourceException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        return toResponse(findOrThrow(username));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findOrThrow(username);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        User saved = userRepository.save(user);
        auditLogHelper.log(username, "PROFILE_UPDATED", "Profile details updated");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProfileResponse submitKyc(String username, KycSubmitRequest request) {
        User user = findOrThrow(username);

        if (user.getKycStatus() == KycStatus.VERIFIED) {
            throw new AccountOperationException("KYC is already verified for this account");
        }

        user.setPanNumber(request.getPanNumber());
        user.setAadhaarLast4(request.getAadhaarNumber().substring(8)); // store only last 4 digits
        user.setDocumentUploaded(request.isDocumentProvided());
        user.setKycStatus(KycStatus.PENDING);

        User saved = userRepository.save(user);

        notificationHelper.notify(user, NotificationType.SECURITY_ALERT,
                "KYC documents submitted and pending verification.");
        auditLogHelper.log(username, "KYC_SUBMITTED", "PAN " + request.getPanNumber() + " submitted for verification");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProfileResponse verifyKyc(Long userId, boolean approve, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getKycStatus() != KycStatus.PENDING) {
            throw new AccountOperationException("KYC is not pending review for this user (" + user.getKycStatus() + ")");
        }

        user.setKycStatus(approve ? KycStatus.VERIFIED : KycStatus.REJECTED);
        User saved = userRepository.save(user);

        notificationHelper.notify(user, NotificationType.SECURITY_ALERT,
                approve ? "KYC verified successfully." : "KYC rejected. Reason: " + (reason != null ? reason : "Not specified"));
        auditLogHelper.log("admin", "KYC_" + (approve ? "VERIFIED" : "REJECTED"), "User " + user.getUsername());

        return toResponse(saved);
    }

    private User findOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private ProfileResponse toResponse(User user) {
        return ProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .documentUploaded(user.isDocumentUploaded())
                .panNumber(user.getPanNumber())
                .aadhaarLast4(user.getAadhaarLast4())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
