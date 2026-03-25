package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
@Getter
@Setter
public class Session extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, unique = true)
    public String token;

    @Column(name = "refresh_token", unique = true)
    public String refreshToken;

    @Column(name = "device_info", length = 500)
    public String deviceInfo;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent", length = 500)
    public String userAgent;

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @Column(name = "refresh_expires_at")
    public LocalDateTime refreshExpiresAt;

    @Column(name = "last_activity_at")
    public LocalDateTime lastActivityAt;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @Column(name = "logout_at")
    public LocalDateTime logoutAt;

    @Column(name = "logout_reason", length = 100)
    public String logoutReason;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public static Session findByToken(String token) {
        return find("token", token).firstResult();
    }

    public static Session findByRefreshToken(String refreshToken) {
        return find("refreshToken", refreshToken).firstResult();
    }

    public static java.util.List<Session> findByUserId(UUID userId) {
        return list("userId", userId);
    }

    public static java.util.List<Session> findActiveByUserId(UUID userId) {
        return list("userId = ?1 and isActive = ?2 and expiresAt > ?3",
            userId, true, LocalDateTime.now());
    }

    public static void invalidateByUserId(UUID userId) {
        update("isActive = false, logoutAt = ?1, logoutReason = ?2 where userId = ?3 and isActive = ?4",
            LocalDateTime.now(), "LOGOUT_ALL", userId, true);
    }
}