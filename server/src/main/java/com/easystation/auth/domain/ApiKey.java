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
@Table(name = "api_keys")
@Getter
@Setter
public class ApiKey extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(length = 500)
    public String description;

    @Column(name = "key_hash", unique = true, nullable = false)
    public String keyHash;

    @Column(nullable = false)
    public String secret;

    @Column(name = "expires_at")
    public LocalDateTime expiresAt;

    @Column(nullable = false)
    public boolean enabled = true;

    @Column(name = "permissions", length = 1000)
    public String permissions;

    @Column(name = "ip_whitelist", length = 500)
    public String ipWhitelist;

    @Column(name = "created_by")
    public UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "revoked_at")
    public LocalDateTime revokedAt;

    @Column(name = "revoked_by")
    public UUID revokedBy;

    @Column(name = "revoke_reason", length = 500)
    public String revokeReason;
}