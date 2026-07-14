package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.LoanApplicationRequest;
import com.chandravijay.banking.dto.LoanResponse;

import java.util.List;

public interface LoanService {
    LoanResponse apply(String username, LoanApplicationRequest request);
    List<LoanResponse> listForUser(String username);
    List<LoanResponse> listAll();
    LoanResponse approve(Long loanId);
    LoanResponse reject(Long loanId, String reason);
}
