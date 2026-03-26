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
 * 密码重置令牌实体
 */
@Entity
@Table(name = "sys_password_reset_token")
@Getter
@Setter
public class PasswordResetToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "token_hash", unique = true, nullable = false)
    public String tokenHash;

    @Column
    public String email;

    @Column(length = 50)
    public String phone;

    @Column(name = "expires_at", nullable = false)
    public LocalDateTime expiresAt;

    @Column(name = "used_at")
    public LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * 检查令牌是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 检查令牌是否已使用
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * 检查令牌是否有效
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * 标记令牌为已使用
     */
    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 根据 token hash 查找
     */
    public static PasswordResetToken findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResult();
    }

    /**
     * 查找用户最近的有效重置令牌
     */
    public static PasswordResetToken findValidByUser(UUID userId) {
        return find("user.id = ?1 and usedAt is null and expiresAt > ?2",
            userId, LocalDateTime.now()).firstResult();
    }

    /**
     * 使所有用户的重置令牌失效
     */
    public static int invalidateAllByUser(UUID userId) {
        return update("usedAt = ?1 where user.id = ?2 and usedAt is null",
            LocalDateTime.now(), userId);
    }

    /**
     * 清理过期的重置令牌
     */
    public static long cleanExpiredTokens() {
        return delete("expiresAt < ?1", LocalDateTime.now());
    }
}