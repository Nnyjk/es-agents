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
@Table(name = "sys_api_token")
@Getter
@Setter
public class ApiToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String token;

    @Column(nullable = false)
    public String name;

    @Column(name = "user_id")
    public UUID userId;

    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TokenScope scope;

    @Column(name = "expires_at")
    public LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    public LocalDateTime lastUsedAt;

    @Column(name = "is_revoked")
    public boolean revoked;

    @Column(name = "revoked_at")
    public LocalDateTime revokedAt;

    @Column(name = "revoked_by")
    public String revokedBy;

    @Column(name = "revoked_reason")
    public String revokedReason;

    @Column(name = "created_by")
    public String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}