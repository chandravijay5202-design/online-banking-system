package com.chandravijay.banking.dto;

import lombok.Data;

@Data
public class LoanDecisionRequest {
    /** Only used when rejecting a loan. */
    private String rejectionReason;
}
