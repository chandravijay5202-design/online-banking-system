package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.entity.Notification;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates in-app notifications. This demo does not send real SMS/email — a production
 * deployment would plug an SMS gateway / SMTP client in here behind the same method signatures.
 */
@Service
@RequiredArgsConstructor
public class NotificationHelperService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(User user, NotificationType type, String message) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .read(false)
                .build());
    }
}
