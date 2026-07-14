package com.chandravijay.banking.service.impl;

import com.chandravijay.banking.dto.SupportTicketRequest;
import com.chandravijay.banking.dto.SupportTicketResponse;
import com.chandravijay.banking.dto.TicketUpdateRequest;
import com.chandravijay.banking.entity.NotificationType;
import com.chandravijay.banking.entity.SupportTicket;
import com.chandravijay.banking.entity.User;
import com.chandravijay.banking.exception.ResourceNotFoundException;
import com.chandravijay.banking.repository.SupportTicketRepository;
import com.chandravijay.banking.repository.UserRepository;
import com.chandravijay.banking.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final NotificationHelperService notificationHelper;
    private final AuditLogHelperService auditLogHelper;

    @Override
    @Transactional
    public SupportTicketResponse raise(String username, SupportTicketRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .subject(request.getSubject())
                .description(request.getDescription())
                .build();

        SupportTicket saved = supportTicketRepository.save(ticket);
        auditLogHelper.log(username, "TICKET_RAISED", "Ticket #" + saved.getId() + ": " + request.getSubject());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> listForUser(String username) {
        return supportTicketRepository.findByUserUsernameOrderByCreatedAtDesc(username)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> listAll() {
        return supportTicketRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public SupportTicketResponse updateStatus(Long ticketId, TicketUpdateRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        ticket.setStatus(request.getStatus());
        if (request.getResolutionNote() != null && !request.getResolutionNote().isBlank()) {
            ticket.setResolutionNote(request.getResolutionNote());
        }
        SupportTicket saved = supportTicketRepository.save(ticket);

        notificationHelper.notify(ticket.getUser(), NotificationType.SUPPORT_UPDATE,
                "Ticket #" + ticketId + " (" + ticket.getSubject() + ") status updated to " + request.getStatus());
        auditLogHelper.log("admin", "TICKET_UPDATED", "Ticket #" + ticketId + " → " + request.getStatus());

        return toResponse(saved);
    }

    private SupportTicketResponse toResponse(SupportTicket t) {
        return SupportTicketResponse.builder()
                .id(t.getId())
                .username(t.getUser().getUsername())
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .resolutionNote(t.getResolutionNote())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
