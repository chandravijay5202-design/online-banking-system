package com.chandravijay.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBeneficiaryRequest {
    @NotBlank(message = "Nickname is required")
    private String nickname;

    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    private String ifscCode;
}
