package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.AccountResponse;
import com.chandravijay.banking.dto.AmountRequest;
import com.chandravijay.banking.dto.CreateAccountRequest;
import com.chandravijay.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(Authentication auth,
                                                           @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AccountResponse>> myAccounts(Authentication auth) {
        return ResponseEntity.ok(accountService.getAccountsForCurrentUser(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id, auth.getName(), isAdmin(auth)));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountResponse> deposit(Authentication auth, @PathVariable Long id,
                                                     @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(id, auth.getName(), isAdmin(auth), request));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(Authentication auth, @PathVariable Long id,
                                                      @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(id, auth.getName(), isAdmin(auth), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeAccount(Authentication auth, @PathVariable Long id) {
        accountService.closeAccount(id, auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
