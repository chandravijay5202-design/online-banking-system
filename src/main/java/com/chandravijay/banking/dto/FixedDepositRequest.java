package com.chandravijay.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FixedDepositRequest {

    @NotBlank(message = "Source account number is required")
    private String accountNumber;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum FD amount is 1000")
    private BigDecimal principal;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    private Integer tenureMonths;
}
