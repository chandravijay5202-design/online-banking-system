package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.FixedDepositStatus;
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
public class FixedDepositResponse {
    private Long id;
    private String linkedAccountNumber;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal maturityAmount;
    private FixedDepositStatus status;
    private LocalDateTime startDate;
    private LocalDateTime maturityDate;
}
