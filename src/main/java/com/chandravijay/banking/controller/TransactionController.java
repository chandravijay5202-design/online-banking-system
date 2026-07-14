package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.TransactionResponse;
import com.chandravijay.banking.dto.TransferRequest;
import com.chandravijay.banking.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(Authentication auth,
                                                          @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(auth.getName(), isAdmin(auth), request));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> history(Authentication auth, @PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getHistoryForAccount(accountId, auth.getName(), isAdmin(auth)));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
