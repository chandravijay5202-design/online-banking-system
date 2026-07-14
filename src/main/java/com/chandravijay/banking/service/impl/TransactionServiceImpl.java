package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.TransactionResponse;
import com.chandravijay.banking.dto.TransferRequest;
import com.chandravijay.banking.entity.*;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.InsufficientBalanceException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.AccountRepository;
import com.chandravijay.banking.repository.TransactionRepository;
import com.chandravijay.banking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    /** Simplified, realistic-ish rail limits — mirrors how real NEFT/RTGS/IMPS caps work in India. */
    private static final java.math.BigDecimal IMPS_MAX = new java.math.BigDecimal("500000");
    private static final java.math.BigDecimal RTGS_MIN = new java.math.BigDecimal("200000");
    private static final java.math.BigDecimal LARGE_TXN_FLAG_THRESHOLD = new java.math.BigDecimal("500000");

    @Override
    @Transactional
    public TransactionResponse transfer(String username, boolean isAdmin, TransferRequest request) {
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new AccountOperationException("Source and destination accounts must be different");
        }

        Account from = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.getFromAccountNumber()));
        Account to = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + request.getToAccountNumber()));

        if (!isAdmin && !from.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own the source account");
        }

        if (from.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException("Source account is not active");
        }
        if (to.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException("Destination account is not active");
        }
        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source account " + from.getAccountNumber());
        }

        TransferMode mode = request.getMode() != null ? request.getMode() : TransferMode.SAME_BANK;
        if (mode == TransferMode.IMPS && request.getAmount().compareTo(IMPS_MAX) > 0) {
            throw new AccountOperationException("IMPS transfers are capped at ₹" + IMPS_MAX + " per transaction — use NEFT or RTGS for larger amounts");
        }
        if (mode == TransferMode.RTGS && request.getAmount().compareTo(RTGS_MIN) < 0) {
            throw new AccountOperationException("RTGS transfers require a minimum of ₹" + RTGS_MIN + " — use IMPS or NEFT for smaller amounts");
        }

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));
        accountRepository.save(from);
        accountRepository.save(to);

        Transaction txn = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fromAccount(from)
                .toAccount(to)
                .mode(mode)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(txn);

        notificationHelper.notify(from.getOwner(), NotificationType.DEBIT_ALERT,
                "₹" + request.getAmount() + " sent via " + mode + " to " + to.getAccountNumber()
                        + ". Available balance: ₹" + from.getBalance());
        notificationHelper.notify(to.getOwner(), NotificationType.CREDIT_ALERT,
                "₹" + request.getAmount() + " received via " + mode + " from " + from.getAccountNumber()
                        + ". Available balance: ₹" + to.getBalance());

        boolean flagged = request.getAmount().compareTo(LARGE_TXN_FLAG_THRESHOLD) >= 0;
        auditLogHelper.log(username, "TRANSFER",
                "₹" + request.getAmount() + " via " + mode + " from " + from.getAccountNumber() + " to " + to.getAccountNumber(),
                flagged);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistoryForAccount(Long accountId, String username, boolean isAdmin) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        if (!isAdmin && !account.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this account");
        }

        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId)
                .stream().map(this::toResponse).toList();
    }

    private TransactionResponse toResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .reference(txn.getReference())
                .type(txn.getType())
                .amount(txn.getAmount())
                .fromAccountNumber(txn.getFromAccount() != null ? txn.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(txn.getToAccount() != null ? txn.getToAccount().getAccountNumber() : null)
                .mode(txn.getMode())
                .status(txn.getStatus())
                .description(txn.getDescription())
                .timestamp(txn.getTimestamp())
                .build();
    }
}
