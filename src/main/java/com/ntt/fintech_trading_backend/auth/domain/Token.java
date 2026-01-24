package com.ntt.fintech_trading_backend.auth.domain;


import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, length = 512, nullable = false)
    public String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 50)
    @Builder.Default
    public TokenType tokenType = TokenType.BEARER;

    @Column(nullable = false)
    @Builder.Default
    public boolean revoked = false;

    @Column(nullable = false)
    @Builder.Default
    public boolean expired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_token_user"))
    public User user;
}
