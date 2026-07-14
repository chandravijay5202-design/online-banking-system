package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.LoanStatus;
import com.chandravijay.banking.entity.LoanType;
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
public class LoanResponse {
    private Long id;
    private String borrowerUsername;
    private LoanType loanType;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private LoanStatus status;
    private Integer installmentsPaid;
    private BigDecimal remainingBalance;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;
}
