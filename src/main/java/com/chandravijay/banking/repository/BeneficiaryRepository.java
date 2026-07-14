package com.chandravijay.banking.repository;

import com.chandravijay.banking.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByOwnerUsername(String username);
    Optional<Beneficiary> findByIdAndOwnerUsername(Long id, String username);
}
