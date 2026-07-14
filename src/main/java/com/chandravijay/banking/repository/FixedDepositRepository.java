package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {
    List<FixedDeposit> findByOwnerUsername(String username);
}
