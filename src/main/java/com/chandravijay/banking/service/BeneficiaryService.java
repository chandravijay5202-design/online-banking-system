package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.AddBeneficiaryRequest;
import com.chandravijay.banking.dto.BeneficiaryResponse;
import com.chandravijay.banking.dto.OtpResponse;

import java.util.List;

public interface BeneficiaryService {
    OtpResponse addBeneficiary(String username, AddBeneficiaryRequest request);
    BeneficiaryResponse verifyOtp(String username, Long beneficiaryId, String otp);
    List<BeneficiaryResponse> listForUser(String username);
    void deleteBeneficiary(String username, Long beneficiaryId);
}
