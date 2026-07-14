package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponse {
    private Long id;
    private String username;
    private String subject;
    private String description;
    private TicketStatus status;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
