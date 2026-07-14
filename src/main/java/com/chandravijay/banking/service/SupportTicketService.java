package com.chandravijay.banking.service;

import com.chandravijay.banking.dto.SupportTicketRequest;
import com.chandravijay.banking.dto.SupportTicketResponse;
import com.chandravijay.banking.dto.TicketUpdateRequest;

import java.util.List;

public interface SupportTicketService {
    SupportTicketResponse raise(String username, SupportTicketRequest request);
    List<SupportTicketResponse> listForUser(String username);
    List<SupportTicketResponse> listAll();
    SupportTicketResponse updateStatus(Long ticketId, TicketUpdateRequest request);
}
