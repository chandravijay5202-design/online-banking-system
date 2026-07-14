package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.BillPaymentRequest;
import com.chandravijay.banking.dto.CreditCardApplicationRequest;
import com.chandravijay.banking.dto.CreditCardResponse;
import com.chandravijay.banking.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    @PostMapping("/apply")
    public ResponseEntity<CreditCardResponse> apply(Authentication auth, @Valid @RequestBody CreditCardApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditCardService.apply(auth.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<CreditCardResponse>> myCards(Authentication auth) {
        return ResponseEntity.ok(creditCardService.listForUser(auth.getName()));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<CreditCardResponse> block(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(creditCardService.block(auth.getName(), id));
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<CreditCardResponse> unblock(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(creditCardService.unblock(auth.getName(), id));
    }

    @PostMapping("/{id}/pay-bill")
    public ResponseEntity<CreditCardResponse> payBill(Authentication auth, @PathVariable Long id,
                                                        @Valid @RequestBody BillPaymentRequest request) {
        return ResponseEntity.ok(creditCardService.payBill(auth.getName(), id, request));
    }
}
