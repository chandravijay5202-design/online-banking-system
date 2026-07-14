package com.chandravijay.banking.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;
}
