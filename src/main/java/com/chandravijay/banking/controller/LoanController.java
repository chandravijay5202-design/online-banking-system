package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.LoanApplicationRequest;
import com.chandravijay.banking.dto.LoanResponse;
import com.chandravijay.banking.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> apply(Authentication auth, @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.apply(auth.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LoanResponse>> myLoans(Authentication auth) {
        return ResponseEntity.ok(loanService.listForUser(auth.getName()));
    }
}
