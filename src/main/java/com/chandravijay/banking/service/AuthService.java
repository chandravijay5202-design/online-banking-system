package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.AuthResponse;
import com.chandravijay.banking.dto.ForgotPasswordRequest;
import com.chandravijay.banking.dto.LoginRequest;
import com.chandravijay.banking.dto.OtpResponse;
import com.chandravijay.banking.dto.RegisterRequest;
import com.chandravijay.banking.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    OtpResponse forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
