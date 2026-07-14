package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.AccountResponse;
import com.chandravijay.banking.dto.AmountRequest;
import com.chandravijay.banking.dto.CreateAccountRequest;
import com.chandravijay.banking.entity.Account;
import com.chandravijay.banking.entity.AccountStatus;
import com.chandravijay.banking.entity.Branch;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.Transaction;
import com.chandravijay.banking.entity.TransactionStatus;
import com.chandravijay.banking.entity.TransactionType;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.InsufficientBalanceException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.AccountRepository;
import com.chandravijay.banking.repository.TransactionRepository;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional
    public AccountResponse createAccount(String username, CreateAccountRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Branch branch = request.getBranch() != null ? request.getBranch() : Branch.HYDERABAD_MAIN;

        Account account = Account.builder()
                .accountType(request.getAccountType())
                .balance(request.getOpeningBalance() == null ? BigDecimal.ZERO : request.getOpeningBalance())
                .status(AccountStatus.ACTIVE)
                .owner(user)
                .ifscCode(branch.getIfscCode())
                .branchName(branch.getBranchName())
                .city(branch.getCity())
                .micrCode(branch.getMicrCode())
                .build();

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsForCurrentUser(String username) {
        return accountRepository.findByOwnerUsername(username)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id, String username, boolean isAdmin) {
        Account account = findAccountOrThrow(id);
        assertOwnershipOrAdmin(account, username, isAdmin);
        return toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse deposit(Long id, String username, boolean isAdmin, AmountRequest request) {
        Account account = findAccountOrThrow(id);
        assertOwnershipOrAdmin(account, username, isAdmin);
        assertActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));
        Account saved = accountRepository.save(account);

        transactionRepository.save(Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .toAccount(saved)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build());

        notificationHelper.notify(account.getOwner(), NotificationType.CREDIT_ALERT,
                "₹" + request.getAmount() + " credited to account " + saved.getAccountNumber()
                        + ". Available balance: ₹" + saved.getBalance());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse withdraw(Long id, String username, boolean isAdmin, AmountRequest request) {
        Account account = findAccountOrThrow(id);
        assertOwnershipOrAdmin(account, username, isAdmin);
        assertActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + account.getAccountNumber());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account saved = accountRepository.save(account);

        transactionRepository.save(Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .fromAccount(saved)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build());

        notificationHelper.notify(account.getOwner(), NotificationType.DEBIT_ALERT,
                "₹" + request.getAmount() + " debited from account " + saved.getAccountNumber()
                        + ". Available balance: ₹" + saved.getBalance());

        boolean largeWithdrawal = request.getAmount().compareTo(new BigDecimal("50000")) >= 0;
        auditLogHelper.log(username, "WITHDRAWAL",
                "₹" + request.getAmount() + " withdrawn from " + saved.getAccountNumber(), largeWithdrawal);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void closeAccount(Long id, String username, boolean isAdmin) {
        Account account = findAccountOrThrow(id);
        assertOwnershipOrAdmin(account, username, isAdmin);

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountOperationException("Cannot close an account with a positive balance. Withdraw funds first.");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    private Account findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    private void assertOwnershipOrAdmin(Account account, String username, boolean isAdmin) {
        if (!isAdmin && !account.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this account");
        }
    }

    private void assertActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException("Account " + account.getAccountNumber() + " is not active (" + account.getStatus() + ")");
        }
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .ownerUsername(account.getOwner().getUsername())
                .ifscCode(account.getIfscCode())
                .branchName(account.getBranchName())
                .city(account.getCity())
                .micrCode(account.getMicrCode())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
