package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.AccountType;
import com.chandravijay.banking.entity.Branch;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Opening balance cannot be negative")
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /** Optional — defaults to the Hyderabad Main Branch if not provided. */
    private Branch branch;
}

