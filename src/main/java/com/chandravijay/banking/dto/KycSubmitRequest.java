package com.chandravijay.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KycSubmitRequest {

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must be in the format ABCDE1234F")
    private String panNumber;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar must be exactly 12 digits")
    private String aadhaarNumber;

    /**
     * In a real system this would be a multipart file upload to storage (S3, etc.).
     * This demo just records that a document was "uploaded" without persisting a real file.
     */
    private boolean documentProvided;
}
