package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);
}
