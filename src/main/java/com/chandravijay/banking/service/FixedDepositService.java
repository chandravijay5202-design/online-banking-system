package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.FixedDepositRequest;
import com.chandravijay.banking.dto.FixedDepositResponse;

import java.util.List;

public interface FixedDepositService {
    FixedDepositResponse create(String username, FixedDepositRequest request);
    List<FixedDepositResponse> listForUser(String username);
    FixedDepositResponse closePrematurely(String username, Long fdId);
}
