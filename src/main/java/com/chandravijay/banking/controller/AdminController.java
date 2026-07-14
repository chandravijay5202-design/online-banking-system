package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.AccountResponse;
import com.chandravijay.banking.dto.AuditLogResponse;
import com.chandravijay.banking.dto.CreditCardResponse;
import com.chandravijay.banking.dto.LoanDecisionRequest;
import com.chandravijay.banking.dto.LoanResponse;
import com.chandravijay.banking.dto.ProfileResponse;
import com.chandravijay.banking.dto.SupportTicketResponse;
import com.chandravijay.banking.dto.TicketUpdateRequest;
import com.chandravijay.banking.entity.AuditLog;
import com.chandravijay.banking.entity.CreditCard;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.AuditLogRepository;
import com.chandravijay.banking.repository.CreditCardRepository;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.AccountService;
import com.chandravijay.banking.service.CreditCardService;
import com.chandravijay.banking.service.LoanService;
import com.chandravijay.banking.service.ProfileService;
import com.chandravijay.banking.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final LoanService loanService;
    private final SupportTicketService supportTicketService;
    private final CreditCardService creditCardService;
    private final ProfileService profileService;
    private final AuditLogRepository auditLogRepository;
    private final CreditCardRepository creditCardRepository;

    // Endpoint-level access is enforced in SecurityConfig: /api/admin/** requires ROLE_ADMIN

    // ---------- Accounts & users ----------

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> allAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/unlock")
    @Transactional
    public ResponseEntity<User> unlockUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return ResponseEntity.ok(userRepository.save(user));
    }

    // ---------- KYC ----------

    @PutMapping("/kyc/{userId}/verify")
    public ResponseEntity<ProfileResponse> verifyKyc(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.verifyKyc(userId, true, null));
    }

    @PutMapping("/kyc/{userId}/reject")
    public ResponseEntity<ProfileResponse> rejectKyc(@PathVariable Long userId, @RequestBody(required = false) LoanDecisionRequest body) {
        String reason = body != null ? body.getRejectionReason() : null;
        return ResponseEntity.ok(profileService.verifyKyc(userId, false, reason));
    }

    // ---------- Loans ----------

    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> allLoans() {
        return ResponseEntity.ok(loanService.listAll());
    }

    @PutMapping("/loans/{id}/approve")
    public ResponseEntity<LoanResponse> approveLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.approve(id));
    }

    @PutMapping("/loans/{id}/reject")
    public ResponseEntity<LoanResponse> rejectLoan(@PathVariable Long id, @Valid @RequestBody LoanDecisionRequest request) {
        return ResponseEntity.ok(loanService.reject(id, request.getRejectionReason()));
    }

    // ---------- Support tickets ----------

    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketResponse>> allTickets() {
        return ResponseEntity.ok(supportTicketService.listAll());
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketResponse> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(supportTicketService.updateStatus(id, request));
    }

    // ---------- Credit cards ----------

    @GetMapping("/credit-cards")
    public ResponseEntity<List<CreditCardResponse>> allCreditCards() {
        return ResponseEntity.ok(creditCardRepository.findAll().stream().map(this::toCardResponse).toList());
    }

    @PutMapping("/credit-cards/{id}/approve")
    public ResponseEntity<CreditCardResponse> approveCard(@PathVariable Long id) {
        return ResponseEntity.ok(creditCardService.approve(id));
    }

    // ---------- Audit / fraud logs ----------

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> allAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc()
                .stream().map(this::toAuditResponse).toList());
    }

    @GetMapping("/audit-logs/flagged")
    public ResponseEntity<List<AuditLogResponse>> flaggedAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findByFlaggedTrueOrderByTimestampDesc()
                .stream().map(this::toAuditResponse).toList());
    }

    private AuditLogResponse toAuditResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUsername())
                .action(log.getAction())
                .details(log.getDetails())
                .flagged(log.isFlagged())
                .timestamp(log.getTimestamp())
                .build();
    }

    private CreditCardResponse toCardResponse(CreditCard card) {
        String last4 = card.getCardNumber().substring(card.getCardNumber().length() - 4);
        return CreditCardResponse.builder()
                .id(card.getId())
                .ownerUsername(card.getOwner().getUsername())
                .maskedCardNumber("**** **** **** " + last4)
                .creditLimit(card.getCreditLimit())
                .outstandingBalance(card.getOutstandingBalance())
                .availableCredit(card.getCreditLimit().subtract(card.getOutstandingBalance()))
                .status(card.getStatus())
                .appliedAt(card.getAppliedAt())
                .build();
    }
}
