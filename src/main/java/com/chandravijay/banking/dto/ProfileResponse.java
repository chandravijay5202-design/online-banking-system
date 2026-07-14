package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.KycStatus;
import com.chandravijay.banking.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private KycStatus kycStatus;
    private boolean documentUploaded;
    private String panNumber;
    private String aadhaarLast4;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
