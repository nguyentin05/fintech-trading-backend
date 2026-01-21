package com.ntt.fintech_trading_backend.auth.domain;

import com.ntt.fintech_trading_backend.kyc.domain.UserKyc;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(max = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    @Size(max = 255)
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 255)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    @Size(max = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_role")
    @Builder.Default
    private UserRole role = UserRole.TRADER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_status")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(name = "is_kyc_verified", nullable = false)
    @Builder.Default
    private boolean isKycVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", columnDefinition = "INET")
    private String lastLoginIp;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<UserSession> sessions = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserKyc kyc;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreferences preferences;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<User2FA> twoFactorAuths = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserLoginHistory> loginHistories = new ArrayList<>();
}