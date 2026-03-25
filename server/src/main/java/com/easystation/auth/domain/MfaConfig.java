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
@Table(name = "auth_mfa_config")
@Getter
@Setter
public class MfaConfig extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    public UUID userId;

    @Column(name = "mfa_type", nullable = false, length = 20)
    public String mfaType = "TOTP";

    @Column(name = "secret_key", nullable = false)
    public String secretKey;

    @Column(name = "is_enabled")
    public Boolean isEnabled = false;

    @Column(name = "is_verified")
    public Boolean isVerified = false;

    @Column(name = "backup_codes", columnDefinition = "TEXT")
    public String backupCodes;

    @Column(name = "verified_at")
    public LocalDateTime verifiedAt;

    @Column(name = "last_used_at")
    public LocalDateTime lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}