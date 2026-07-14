package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserUsernameOrderByCreatedAtDesc(String username);
}
