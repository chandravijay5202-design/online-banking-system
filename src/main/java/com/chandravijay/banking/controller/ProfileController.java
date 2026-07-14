package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.KycSubmitRequest;
import com.chandravijay.banking.dto.ProfileResponse;
import com.chandravijay.banking.dto.UpdateProfileRequest;
import com.chandravijay.banking.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication auth) {
        return ResponseEntity.ok(profileService.getProfile(auth.getName()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(auth.getName(), request));
    }

    @PostMapping("/kyc")
    public ResponseEntity<ProfileResponse> submitKyc(Authentication auth, @Valid @RequestBody KycSubmitRequest request) {
        return ResponseEntity.ok(profileService.submitKyc(auth.getName(), request));
    }
}
