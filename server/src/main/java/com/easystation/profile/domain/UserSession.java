package com.easystation.profile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_session")
@Getter
@Setter
public class UserSession extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "token_id", nullable = false)
    public String tokenId;

    @Column(name = "device_info")
    public String deviceInfo;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent", length = 500)
    public String userAgent;

    @Column(name = "login_at")
    public LocalDateTime loginAt;

    @Column(name = "last_activity_at")
    public LocalDateTime lastActivityAt;

    @Column(name = "expires_at")
    public LocalDateTime expiresAt;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}