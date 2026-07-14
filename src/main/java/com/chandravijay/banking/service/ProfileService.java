package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.KycSubmitRequest;
import com.chandravijay.banking.dto.ProfileResponse;
import com.chandravijay.banking.dto.UpdateProfileRequest;

public interface ProfileService {
    ProfileResponse getProfile(String username);
    ProfileResponse updateProfile(String username, UpdateProfileRequest request);
    ProfileResponse submitKyc(String username, KycSubmitRequest request);
    ProfileResponse verifyKyc(Long userId, boolean approve, String reason);
}
