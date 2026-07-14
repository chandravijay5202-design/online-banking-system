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
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false, length = 20)
    private String ifscCode;

    @Column(nullable = false, length = 100)
    private String branchName;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 9)
    private String micrCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User owner;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.accountNumber == null) {
            // Real Indian bank account numbers are purely numeric, typically 11-16 digits.
            // Build one from this account's branch code + time-based digits + a random pair,
            // so numbers are realistic-looking and collision-resistant without being sequential.
            String branchDigits = (this.ifscCode != null && this.ifscCode.length() >= 11)
                    ? this.ifscCode.substring(5) // digits after the 4-letter code + '0'
                    : "000000";
            String timeDigits = String.format("%06d", System.currentTimeMillis() % 1_000_000L);
            String randomSuffix = String.valueOf((int) (Math.random() * 90) + 10);
            this.accountNumber = branchDigits + timeDigits + randomSuffix;
        }
    }
}
