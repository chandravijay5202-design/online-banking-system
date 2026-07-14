package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.AddBeneficiaryRequest;
import com.chandravijay.banking.dto.BeneficiaryResponse;
import com.chandravijay.banking.dto.OtpResponse;
import com.chandravijay.banking.entity.Beneficiary;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.AccountOperationException;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.BeneficiaryRepository;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private static final int OTP_VALIDITY_SECONDS = 300; // 5 minutes

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional
    public OtpResponse addBeneficiary(String username, AddBeneficiaryRequest request) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String otp = generateOtp();

        Beneficiary beneficiary = Beneficiary.builder()
                .owner(owner)
                .nickname(request.getNickname())
                .beneficiaryName(request.getBeneficiaryName())
                .accountNumber(request.getAccountNumber())
                .ifscCode(request.getIfscCode())
                .active(false)
                .otpCode(otp)
                .otpExpiresAt(LocalDateTime.now().plusSeconds(OTP_VALIDITY_SECONDS))
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        notificationHelper.notify(owner, NotificationType.SECURITY_ALERT,
                "OTP requested to add beneficiary \"" + request.getNickname() + "\". Do not share this code with anyone.");
        auditLogHelper.log(username, "BENEFICIARY_ADD_REQUESTED",
                "Beneficiary add requested: " + request.getAccountNumber() + " — pending OTP approval (id=" + saved.getId() + ")");

        return OtpResponse.builder()
                .message("OTP generated. In production this would be sent via SMS/email — verify it against POST /api/beneficiaries/" + saved.getId() + "/verify")
                .devOnlyOtp(otp)
                .expiresInSeconds(OTP_VALIDITY_SECONDS)
                .build();
    }

    @Override
    @Transactional
    public BeneficiaryResponse verifyOtp(String username, Long beneficiaryId, String otp) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwnerUsername(beneficiaryId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        if (beneficiary.isActive()) {
            throw new AccountOperationException("Beneficiary is already verified");
        }
        if (beneficiary.getOtpExpiresAt() == null || beneficiary.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccountOperationException("OTP has expired — remove this beneficiary and add it again");
        }
        if (!beneficiary.getOtpCode().equals(otp)) {
            throw new AccountOperationException("Incorrect OTP");
        }

        beneficiary.setActive(true);
        beneficiary.setOtpCode(null);
        beneficiary.setOtpExpiresAt(null);
        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        auditLogHelper.log(username, "BENEFICIARY_VERIFIED", "Beneficiary verified: " + saved.getAccountNumber());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> listForUser(String username) {
        return beneficiaryRepository.findByOwnerUsername(username)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteBeneficiary(String username, Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwnerUsername(beneficiaryId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        beneficiaryRepository.delete(beneficiary);
        auditLogHelper.log(username, "BENEFICIARY_DELETED", "Beneficiary removed: " + beneficiary.getAccountNumber());
    }

    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private BeneficiaryResponse toResponse(Beneficiary b) {
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .nickname(b.getNickname())
                .beneficiaryName(b.getBeneficiaryName())
                .accountNumber(b.getAccountNumber())
                .ifscCode(b.getIfscCode())
                .active(b.isActive())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
