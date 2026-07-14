package com.chandravijay.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {
    private Long id;
    private String nickname;
    private String beneficiaryName;
    private String accountNumber;
    private String ifscCode;
    private boolean active;
    private LocalDateTime createdAt;
}
