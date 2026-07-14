package com.chandravijay.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ROLE_CUSTOMER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // ---------- Account locking ----------
    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(length = 10)
    private String resetOtpCode;

    private LocalDateTime resetOtpExpiresAt;

    private LocalDateTime lastLoginAt;

    // ---------- KYC ----------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

    @Column(length = 20)
    private String panNumber;

    /** Stored masked (last 4 digits only) for display; never store full Aadhaar in real systems. */
    @Column(length = 20)
    private String aadhaarLast4;

    @Column(nullable = false)
    @Builder.Default
    private boolean documentUploaded = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Account> accounts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
