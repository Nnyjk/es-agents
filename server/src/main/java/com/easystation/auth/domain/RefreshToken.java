package com.easystation.auth.domain;

import com.easystation.system.domain.User;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 刷新令牌实体
 */
@Entity
@Table(name = "sys_refresh_token")
@Getter
@Setter
public class RefreshToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "token_hash", unique = true, nullable = false)
    public String tokenHash;

    @Column(name = "device_info")
    public String deviceInfo;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent")
    public String userAgent;

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "last_used_at")
    public LocalDateTime lastUsedAt;

    @Column(name = "is_revoked")
    public boolean revoked = false;

    /**
     * 检查令牌是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 检查令牌是否有效
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    /**
     * 根据用户查找所有有效的刷新令牌
     */
    public static java.util.List<RefreshToken> findValidByUser(UUID userId) {
        return find("user.id = ?1 and revoked = false and expiresAt > ?2",
            userId, LocalDateTime.now()).list();
    }

    /**
     * 根据 token hash 查找
     */
    public static RefreshToken findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResult();
    }

    /**
     * 撤销用户的所有刷新令牌
     */
    public static int revokeAllByUser(UUID userId) {
        return update("revoked = true where user.id = ?1", userId);
    }
}