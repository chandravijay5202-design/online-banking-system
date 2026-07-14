package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByFlaggedTrueOrderByTimestampDesc();
}
