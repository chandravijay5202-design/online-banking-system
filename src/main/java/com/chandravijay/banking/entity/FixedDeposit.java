package com.chandravijay.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_deposits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_account_id", nullable = false)
    @JsonIgnore
    private Account linkedAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    /** Annual interest rate as a percentage, e.g. 7.25 for 7.25% */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maturityAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FixedDepositStatus status = FixedDepositStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime maturityDate;

    @PrePersist
    protected void onCreate() {
        this.startDate = LocalDateTime.now();
        this.maturityDate = this.startDate.plusMonths(this.tenureMonths);
    }
}
