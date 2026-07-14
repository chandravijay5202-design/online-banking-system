package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.CreditCardStatus;
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
public class CreditCardResponse {
    private Long id;
    private String ownerUsername;
    /** Masked — only the last 4 digits, like a real bank shows. */
    private String maskedCardNumber;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private BigDecimal availableCredit;
    private CreditCardStatus status;
    private LocalDateTime appliedAt;
}
