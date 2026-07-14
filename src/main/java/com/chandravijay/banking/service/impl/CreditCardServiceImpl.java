package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.BillPaymentRequest;
import com.chandravijay.banking.dto.CreditCardApplicationRequest;
import com.chandravijay.banking.dto.CreditCardResponse;
import com.chandravijay.banking.entity.*;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.InsufficientBalanceException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.AccountRepository;
import com.chandravijay.banking.repository.CreditCardRepository;
import com.chandravijay.banking.repository.TransactionRepository;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional
    public CreditCardResponse apply(String username, CreditCardApplicationRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        CreditCard card = CreditCard.builder()
                .owner(user)
                .creditLimit(request.getRequestedLimit())
                .outstandingBalance(BigDecimal.ZERO)
                .status(CreditCardStatus.APPLIED)
                .build();

        CreditCard saved = creditCardRepository.save(card);

        notificationHelper.notify(user, NotificationType.CARD_UPDATE,
                "Credit card application submitted — requested limit ₹" + request.getRequestedLimit());
        auditLogHelper.log(username, "CARD_APPLIED", "Credit card applied — limit ₹" + request.getRequestedLimit());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCardResponse> listForUser(String username) {
        return creditCardRepository.findByOwnerUsername(username).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CreditCardResponse block(String username, Long cardId) {
        CreditCard card = findOwnedOrThrow(cardId, username);
        card.setStatus(CreditCardStatus.BLOCKED);
        CreditCard saved = creditCardRepository.save(card);

        notificationHelper.notify(card.getOwner(), NotificationType.SECURITY_ALERT,
                "Card ending " + last4(card.getCardNumber()) + " has been blocked.");
        auditLogHelper.log(username, "CARD_BLOCKED", "Card #" + cardId + " blocked by owner");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public CreditCardResponse unblock(String username, Long cardId) {
        CreditCard card = findOwnedOrThrow(cardId, username);
        if (card.getStatus() != CreditCardStatus.BLOCKED) {
            throw new AccountOperationException("Card is not currently blocked");
        }
        card.setStatus(CreditCardStatus.APPROVED);
        CreditCard saved = creditCardRepository.save(card);

        notificationHelper.notify(card.getOwner(), NotificationType.CARD_UPDATE,
                "Card ending " + last4(card.getCardNumber()) + " has been unblocked.");
        auditLogHelper.log(username, "CARD_UNBLOCKED", "Card #" + cardId + " unblocked by owner");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public CreditCardResponse payBill(String username, Long cardId, BillPaymentRequest request) {
        CreditCard card = findOwnedOrThrow(cardId, username);
        if (card.getStatus() != CreditCardStatus.APPROVED) {
            throw new AccountOperationException("Card is not active (" + card.getStatus() + ")");
        }
        if (card.getOutstandingBalance().compareTo(request.getAmount()) < 0) {
            throw new AccountOperationException("Payment amount exceeds outstanding balance of ₹" + card.getOutstandingBalance());
        }

        Account account = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getFromAccountNumber()));
        if (!account.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this account");
        }
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in " + account.getAccountNumber());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        card.setOutstandingBalance(card.getOutstandingBalance().subtract(request.getAmount()));
        CreditCard savedCard = creditCardRepository.save(card);

        transactionRepository.save(Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .fromAccount(account)
                .status(TransactionStatus.SUCCESS)
                .description("Credit card bill payment (card #" + cardId + ")")
                .build());

        notificationHelper.notify(card.getOwner(), NotificationType.CARD_UPDATE,
                "₹" + request.getAmount() + " paid toward card ending " + last4(card.getCardNumber())
                        + ". Remaining outstanding: ₹" + savedCard.getOutstandingBalance());
        auditLogHelper.log(username, "CARD_BILL_PAID", "₹" + request.getAmount() + " paid on card #" + cardId);

        return toResponse(savedCard);
    }

    @Override
    @Transactional
    public CreditCardResponse approve(Long cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
        if (card.getStatus() != CreditCardStatus.APPLIED) {
            throw new AccountOperationException("Card has already been decided (" + card.getStatus() + ")");
        }
        card.setStatus(CreditCardStatus.APPROVED);
        CreditCard saved = creditCardRepository.save(card);

        notificationHelper.notify(card.getOwner(), NotificationType.CARD_UPDATE,
                "Your credit card application was approved. Card ending " + last4(saved.getCardNumber()) + " is now active.");
        auditLogHelper.log("admin", "CARD_APPROVED", "Card #" + cardId + " approved");

        return toResponse(saved);
    }

    private CreditCard findOwnedOrThrow(Long cardId, String username) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this card");
        }
        return card;
    }

    private String last4(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private CreditCardResponse toResponse(CreditCard card) {
        return CreditCardResponse.builder()
                .id(card.getId())
                .ownerUsername(card.getOwner().getUsername())
                .maskedCardNumber("**** **** **** " + last4(card.getCardNumber()))
                .creditLimit(card.getCreditLimit())
                .outstandingBalance(card.getOutstandingBalance())
                .availableCredit(card.getCreditLimit().subtract(card.getOutstandingBalance()))
                .status(card.getStatus())
                .appliedAt(card.getAppliedAt())
                .build();
    }
}
