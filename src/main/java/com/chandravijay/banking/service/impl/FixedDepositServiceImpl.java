package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.FixedDepositRequest;
import com.chandravijay.banking.dto.FixedDepositResponse;
import com.chandravijay.banking.entity.*;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.InsufficientBalanceException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.AccountRepository;
import com.chandravijay.banking.repository.FixedDepositRepository;
import com.chandravijay.banking.repository.TransactionRepository;
import com.chandravijay.banking.service.FixedDepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FixedDepositServiceImpl implements FixedDepositService {

    /** Flat indicative FD interest rate for this demo — real banks tier this by tenure. */
    private static final BigDecimal FD_ANNUAL_RATE = new BigDecimal("7.25");
    /** Interest forfeited (halved) on premature closure — a simplified stand-in for a real penalty schedule. */
    private static final BigDecimal PREMATURE_PENALTY_RATE = new BigDecimal("0.50");

    private final FixedDepositRepository fixedDepositRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional
    public FixedDepositResponse create(String username, FixedDepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountNumber()));

        if (!account.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this account");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountOperationException("Account is not active");
        }
        if (account.getBalance().compareTo(request.getPrincipal()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance to open this fixed deposit");
        }

        // Money moves out of the linked savings/current account into the FD.
        account.setBalance(account.getBalance().subtract(request.getPrincipal()));
        accountRepository.save(account);

        BigDecimal maturityAmount = calculateMaturity(request.getPrincipal(), FD_ANNUAL_RATE, request.getTenureMonths());

        FixedDeposit fd = FixedDeposit.builder()
                .owner(account.getOwner())
                .linkedAccount(account)
                .principal(request.getPrincipal())
                .interestRate(FD_ANNUAL_RATE)
                .tenureMonths(request.getTenureMonths())
                .maturityAmount(maturityAmount)
                .status(FixedDepositStatus.ACTIVE)
                .build();

        FixedDeposit saved = fixedDepositRepository.save(fd);

        transactionRepository.save(Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getPrincipal())
                .fromAccount(account)
                .status(TransactionStatus.SUCCESS)
                .description("Fixed deposit opened (FD #" + saved.getId() + ")")
                .build());

        notificationHelper.notify(account.getOwner(), NotificationType.FD_UPDATE,
                "Fixed deposit of ₹" + request.getPrincipal() + " opened for " + request.getTenureMonths()
                        + " months. Maturity amount: ₹" + maturityAmount);
        auditLogHelper.log(username, "FD_OPENED", "₹" + request.getPrincipal() + " FD opened from " + account.getAccountNumber());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FixedDepositResponse> listForUser(String username) {
        return fixedDepositRepository.findByOwnerUsername(username).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public FixedDepositResponse closePrematurely(String username, Long fdId) {
        FixedDeposit fd = fixedDepositRepository.findById(fdId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixed deposit not found"));

        if (!fd.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this fixed deposit");
        }
        if (fd.getStatus() != FixedDepositStatus.ACTIVE) {
            throw new AccountOperationException("This fixed deposit is not active (" + fd.getStatus() + ")");
        }

        // Premature closure pays back principal + a penalized (halved) portion of the earned interest.
        BigDecimal earnedInterest = fd.getMaturityAmount().subtract(fd.getPrincipal());
        BigDecimal penalizedInterest = earnedInterest.multiply(PREMATURE_PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal payout = fd.getPrincipal().add(penalizedInterest);

        Account account = fd.getLinkedAccount();
        account.setBalance(account.getBalance().add(payout));
        accountRepository.save(account);

        fd.setStatus(FixedDepositStatus.CLOSED_PREMATURE);
        FixedDeposit saved = fixedDepositRepository.save(fd);

        transactionRepository.save(Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(payout)
                .toAccount(account)
                .status(TransactionStatus.SUCCESS)
                .description("Fixed deposit closed prematurely (FD #" + fdId + "), penalty applied")
                .build());

        notificationHelper.notify(fd.getOwner(), NotificationType.FD_UPDATE,
                "Fixed deposit #" + fdId + " closed prematurely. ₹" + payout + " credited (penalty applied to interest).");
        auditLogHelper.log(username, "FD_CLOSED_PREMATURE", "FD #" + fdId + " closed early, payout ₹" + payout);

        return toResponse(saved);
    }

    /** Simple (non-compounding) interest for this demo: M = P × (1 + r × t) */
    private BigDecimal calculateMaturity(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        BigDecimal years = new BigDecimal(tenureMonths).divide(new BigDecimal(12), 6, RoundingMode.HALF_UP);
        BigDecimal interest = principal
                .multiply(annualRatePercent)
                .multiply(years)
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        return principal.add(interest);
    }

    private FixedDepositResponse toResponse(FixedDeposit fd) {
        return FixedDepositResponse.builder()
                .id(fd.getId())
                .linkedAccountNumber(fd.getLinkedAccount().getAccountNumber())
                .principal(fd.getPrincipal())
                .interestRate(fd.getInterestRate())
                .tenureMonths(fd.getTenureMonths())
                .maturityAmount(fd.getMaturityAmount())
                .status(fd.getStatus())
                .startDate(fd.getStartDate())
                .maturityDate(fd.getMaturityDate())
                .build();
    }
}
