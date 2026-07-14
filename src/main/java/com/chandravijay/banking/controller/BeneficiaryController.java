package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.AddBeneficiaryRequest;
import com.chandravijay.banking.dto.BeneficiaryResponse;
import com.chandravijay.banking.dto.OtpResponse;
import com.chandravijay.banking.dto.OtpVerifyRequest;
import com.chandravijay.banking.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<OtpResponse> add(Authentication auth, @Valid @RequestBody AddBeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.addBeneficiary(auth.getName(), request));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<BeneficiaryResponse> verify(Authentication auth, @PathVariable Long id,
                                                        @Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(beneficiaryService.verifyOtp(auth.getName(), id, request.getOtp()));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> list(Authentication auth) {
        return ResponseEntity.ok(beneficiaryService.listForUser(auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
