package com.chandravijay.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank(message = "OTP is required")
    private String otp;
}
