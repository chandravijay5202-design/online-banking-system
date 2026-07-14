package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.FixedDepositRequest;
import com.chandravijay.banking.dto.FixedDepositResponse;
import com.chandravijay.banking.service.FixedDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixed-deposits")
@RequiredArgsConstructor
public class FixedDepositController {

    private final FixedDepositService fixedDepositService;

    @PostMapping
    public ResponseEntity<FixedDepositResponse> create(Authentication auth, @Valid @RequestBody FixedDepositRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fixedDepositService.create(auth.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<FixedDepositResponse>> myDeposits(Authentication auth) {
        return ResponseEntity.ok(fixedDepositService.listForUser(auth.getName()));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<FixedDepositResponse> close(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(fixedDepositService.closePrematurely(auth.getName(), id));
    }
}
