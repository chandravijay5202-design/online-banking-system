package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.AccountResponse;
import com.chandravijay.banking.dto.AmountRequest;
import com.chandravijay.banking.dto.CreateAccountRequest;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(String username, CreateAccountRequest request);
    List<AccountResponse> getAccountsForCurrentUser(String username);
    List<AccountResponse> getAllAccounts();
    AccountResponse getAccountById(Long id, String username, boolean isAdmin);
    AccountResponse deposit(Long id, String username, boolean isAdmin, AmountRequest request);
    AccountResponse withdraw(Long id, String username, boolean isAdmin, AmountRequest request);
    void closeAccount(Long id, String username, boolean isAdmin);
}
