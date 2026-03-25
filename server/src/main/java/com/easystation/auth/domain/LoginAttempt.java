package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 登录尝试记录实体
 */
@Entity
@Table(name = "sys_login_attempt")
@Getter
@Setter
public class LoginAttempt extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(length = 255)
    public String username;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent")
    public String userAgent;

    @Column(nullable = false)
    public boolean success;

    @Column(name = "fail_reason")
    public String failReason;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * 记录成功的登录尝试
     */
    public static LoginAttempt success(String username, String ipAddress, String userAgent) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.username = username;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        attempt.success = true;
        attempt.persist();
        return attempt;
    }

    /**
     * 记录失败的登录尝试
     */
    public static LoginAttempt fail(String username, String ipAddress, String userAgent, String reason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.username = username;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        attempt.success = false;
        attempt.failReason = reason;
        attempt.persist();
        return attempt;
    }

    /**
     * 统计指定时间范围内某用户的失败登录次数
     */
    public static long countFailedAttempts(String username, LocalDateTime since) {
        return count("username = ?1 and success = false and createdAt > ?2", username, since);
    }

    /**
     * 统计指定时间范围内某 IP 的失败登录次数
     */
    public static long countFailedAttemptsByIp(String ipAddress, LocalDateTime since) {
        return count("ipAddress = ?1 and success = false and createdAt > ?2", ipAddress, since);
    }

    /**
     * 清理过期的登录记录（保留最近 30 天）
     */
    public static long cleanOldRecords(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        return delete("createdAt < ?1", cutoff);
    }
}