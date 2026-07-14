package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerUsername(String username);
}
