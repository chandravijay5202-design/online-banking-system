package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.BillPaymentRequest;
import com.chandravijay.banking.dto.CreditCardApplicationRequest;
import com.chandravijay.banking.dto.CreditCardResponse;

import java.util.List;

public interface CreditCardService {
    CreditCardResponse apply(String username, CreditCardApplicationRequest request);
    List<CreditCardResponse> listForUser(String username);
    CreditCardResponse block(String username, Long cardId);
    CreditCardResponse unblock(String username, Long cardId);
    CreditCardResponse payBill(String username, Long cardId, BillPaymentRequest request);
    CreditCardResponse approve(Long cardId);
}
