package com.chandravijay.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardApplicationRequest {

    @NotNull(message = "Requested credit limit is required")
    @DecimalMin(value = "5000.0", message = "Minimum credit limit is 5000")
    private BigDecimal requestedLimit;
}
