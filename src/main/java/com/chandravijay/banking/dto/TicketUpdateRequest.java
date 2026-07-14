package com.chandravijay.banking.dto;

import com.chandravijay.banking.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketUpdateRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    private String resolutionNote;
}
