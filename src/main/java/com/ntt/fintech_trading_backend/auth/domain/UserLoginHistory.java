package com.ntt.fintech_trading_backend.auth.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "user_login_history", indexes = {
        @Index(name = "idx_login_history_user_id", columnList = "user_id, created_at")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "ip_address", nullable = false, columnDefinition = "INET")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Size(max = 500)
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @NotNull
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    @Column(name = "failure_reason", columnDefinition = "INET")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
}
