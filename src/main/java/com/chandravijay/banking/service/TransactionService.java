package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.TransactionResponse;
import com.chandravijay.banking.dto.TransferRequest;

import java.util.List;

public interface TransactionService {
    TransactionResponse transfer(String username, boolean isAdmin, TransferRequest request);
    List<TransactionResponse> getHistoryForAccount(Long accountId, String username, boolean isAdmin);
}
