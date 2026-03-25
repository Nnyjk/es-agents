package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_password_reset")
@Getter
@Setter
public class PasswordReset extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, unique = true)
    public String token;

    @Column(name = "reset_type", length = 20)
    public String resetType = "FORGOT";

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @Column(name = "is_used")
    public Boolean isUsed = false;

    @Column(name = "used_at")
    public LocalDateTime usedAt;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    public static PasswordReset findByToken(String token) {
        return find("token", token).firstResult();
    }

    public static PasswordReset findValidToken(String token) {
        return find("token = ?1 and isUsed = ?2 and expiresAt > ?3",
            token, false, LocalDateTime.now()).firstResult();
    }
}