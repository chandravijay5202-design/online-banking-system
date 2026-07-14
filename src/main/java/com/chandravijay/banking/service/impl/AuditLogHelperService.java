package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.entity.AuditLog;
import com.chandravijay.banking.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogHelperService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String username, String action, String details) {
        log(username, action, details, false);
    }

    @Transactional
    public void log(String username, String action, String details, boolean flagged) {
        auditLogRepository.save(AuditLog.builder()
                .username(username)
                .action(action)
                .details(details)
                .flagged(flagged)
                .build());
    }
}
