package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.LoanApplicationRequest;
import com.chandravijay.banking.dto.LoanResponse;
import com.chandravijay.banking.entity.Loan;
import com.chandravijay.banking.entity.LoanStatus;
import com.chandravijay.banking.entity.LoanType;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.LoanRepository;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    /** Simplified indicative annual rates by loan type — a real bank would price these per-applicant. */
    private static final Map<LoanType, BigDecimal> RATES = Map.of(
            LoanType.PERSONAL, new BigDecimal("12.00"),
            LoanType.HOME, new BigDecimal("8.50"),
            LoanType.VEHICLE, new BigDecimal("9.50")
    );

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional
    public LoanResponse apply(String username, LoanApplicationRequest request) {
        User borrower = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        BigDecimal annualRate = RATES.get(request.getLoanType());
        BigDecimal emi = calculateEmi(request.getPrincipal(), annualRate, request.getTenureMonths());

        Loan loan = Loan.builder()
                .borrower(borrower)
                .loanType(request.getLoanType())
                .principal(request.getPrincipal())
                .interestRate(annualRate)
                .tenureMonths(request.getTenureMonths())
                .emiAmount(emi)
                .status(LoanStatus.PENDING)
                .installmentsPaid(0)
                .build();

        Loan saved = loanRepository.save(loan);

        notificationHelper.notify(borrower, NotificationType.LOAN_UPDATE,
                request.getLoanType() + " loan application submitted for ₹" + request.getPrincipal() + ". Estimated EMI: ₹" + emi);
        auditLogHelper.log(username, "LOAN_APPLIED", request.getLoanType() + " loan applied — ₹" + request.getPrincipal());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> listForUser(String username) {
        return loanRepository.findByBorrowerUsername(username).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> listAll() {
        return loanRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public LoanResponse approve(Long loanId) {
        Loan loan = findOrThrow(loanId);
        assertPending(loan);

        loan.setStatus(LoanStatus.APPROVED);
        loan.setDecidedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);

        notificationHelper.notify(loan.getBorrower(), NotificationType.LOAN_UPDATE,
                "Your " + loan.getLoanType() + " loan for ₹" + loan.getPrincipal() + " has been approved. EMI: ₹" + loan.getEmiAmount());
        auditLogHelper.log("admin", "LOAN_APPROVED", "Loan #" + loanId + " approved for " + loan.getBorrower().getUsername());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public LoanResponse reject(Long loanId, String reason) {
        Loan loan = findOrThrow(loanId);
        assertPending(loan);

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(reason != null && !reason.isBlank() ? reason : "Not specified");
        loan.setDecidedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);

        notificationHelper.notify(loan.getBorrower(), NotificationType.LOAN_UPDATE,
                "Your " + loan.getLoanType() + " loan application was rejected. Reason: " + loan.getRejectionReason());
        auditLogHelper.log("admin", "LOAN_REJECTED", "Loan #" + loanId + " rejected for " + loan.getBorrower().getUsername());

        return toResponse(saved);
    }

    private void assertPending(Loan loan) {
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AccountOperationException("This loan has already been decided (" + loan.getStatus() + ")");
        }
    }

    private Loan findOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    /** Standard reducing-balance EMI formula: E = P·r·(1+r)^n / ((1+r)^n − 1) */
    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        BigDecimal monthlyRate = annualRatePercent.divide(new BigDecimal("1200"), MathContext.DECIMAL64);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, MathContext.DECIMAL64);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private LoanResponse toResponse(Loan loan) {
        BigDecimal remaining = loan.getEmiAmount()
                .multiply(new BigDecimal(loan.getTenureMonths() - loan.getInstallmentsPaid()));

        return LoanResponse.builder()
                .id(loan.getId())
                .borrowerUsername(loan.getBorrower().getUsername())
                .loanType(loan.getLoanType())
                .principal(loan.getPrincipal())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .status(loan.getStatus())
                .installmentsPaid(loan.getInstallmentsPaid())
                .remainingBalance(loan.getStatus() == LoanStatus.APPROVED ? remaining : null)
                .rejectionReason(loan.getRejectionReason())
                .appliedAt(loan.getAppliedAt())
                .decidedAt(loan.getDecidedAt())
                .build();
    }
}
