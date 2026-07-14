package com.chandravijay.banking.service;

public interface StatementService {
    byte[] generateStatementPdf(Long accountId, String username, boolean isAdmin);
}
