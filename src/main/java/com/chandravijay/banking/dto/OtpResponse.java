package com.chandravijay.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponse {
    private String message;
    /** Only present because this demo has no real SMS/email gateway wired up — a real deployment would never return this. */
    private String devOnlyOtp;
    private int expiresInSeconds;
}
