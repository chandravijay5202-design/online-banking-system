package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.AccountStatus;
import com.chandravijay.banking.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private String ownerUsername;
    private String ifscCode;
    private String branchName;
    private String city;
    private String micrCode;
    private LocalDateTime createdAt;
}
