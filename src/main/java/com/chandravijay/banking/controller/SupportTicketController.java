package com.chandravijay.banking.controller;

import com.chandravijay.banking.dto.SupportTicketRequest;
import com.chandravijay.banking.dto.SupportTicketResponse;
import com.chandravijay.banking.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<SupportTicketResponse> raise(Authentication auth, @Valid @RequestBody SupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportTicketService.raise(auth.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SupportTicketResponse>> myTickets(Authentication auth) {
        return ResponseEntity.ok(supportTicketService.listForUser(auth.getName()));
    }
}
