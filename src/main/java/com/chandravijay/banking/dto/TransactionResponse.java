package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.TransactionStatus;
import com.chandravijay.banking.entity.TransactionType;
import com.chandravijay.banking.entity.TransferMode;
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
public class TransactionResponse {
    private Long id;
    private String reference;
    private TransactionType type;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private TransferMode mode;
    private TransactionStatus status;
    private String description;
    private LocalDateTime timestamp;
}
