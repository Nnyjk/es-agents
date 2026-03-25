package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_login_audit")
@Getter
@Setter
public class LoginAudit extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id")
    public UUID userId;

    @Column(length = 100)
    public String username;

    @Column(nullable = false, length = 20)
    public String action;

    @Column(length = 20)
    public String result;

    @Column(name = "login_method", length = 20)
    public String loginMethod;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent", length = 500)
    public String userAgent;

    @Column(name = "device_info", length = 500)
    public String deviceInfo;

    @Column(name = "failure_reason", length = 200)
    public String failureReason;

    @Column(name = "session_id")
    public String sessionId;

    @Column(columnDefinition = "TEXT")
    public String details;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    public static java.util.List<LoginAudit> findByUserId(UUID userId, int limit) {
        return find("userId = ?1 order by createdAt desc", userId).range(0, limit - 1).list();
    }

    public static long countFailedAttempts(String username, LocalDateTime since) {
        return count("username = ?1 and action = ?2 and result = ?3 and createdAt > ?4",
            username, "LOGIN", "FAILED", since);
    }
}