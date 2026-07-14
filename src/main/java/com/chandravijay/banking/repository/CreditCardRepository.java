package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    List<CreditCard> findByOwnerUsername(String username);
}
