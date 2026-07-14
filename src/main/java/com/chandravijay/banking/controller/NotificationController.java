package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.NotificationResponse;
import com.chandravijay.banking.entity.Notification;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(Authentication auth) {
        List<NotificationResponse> notifications = notificationRepository
                .findByUserUsernameOrderByCreatedAtDesc(auth.getName())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    @Transactional
    public ResponseEntity<NotificationResponse> markRead(Authentication auth, @PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getUsername().equals(auth.getName())) {
            throw new AccessDeniedException("This notification does not belong to you");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return ResponseEntity.ok(toResponse(saved));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
