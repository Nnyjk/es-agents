package com.easystation.audit.domain;

import com.easystation.audit.enums.AuditAction;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 审计告警配置
 */
@Entity
@Table(name = "audit_alert_config")
@Getter
@Setter
public class AuditAlertConfig extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 告警名称
     */
    @Column(nullable = false)
    public String name;

    /**
     * 告警描述
     */
    @Column(length = 500)
    public String description;

    /**
     * 告警类型: SENSITIVE_OPERATION, FAILED_OPERATION, ABNORMAL_IP, FREQUENT_ACCESS
     */
    @Column(nullable = false)
    public String alertType;

    /**
     * 敏感操作列表
     */
    @ElementCollection
    @CollectionTable(name = "audit_alert_sensitive_actions", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    public List<AuditAction> sensitiveActions = new ArrayList<>();

    /**
     * 白名单用户列表
     */
    @ElementCollection
    @CollectionTable(name = "audit_alert_whitelist_users", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "username")
    public List<String> whitelistUsers = new ArrayList<>();

    /**
     * 失败阈值
     */
    public Integer failureThreshold;

    /**
     * 时间窗口（分钟）
     */
    public Integer timeWindowMinutes;

    /**
     * 通知渠道列表
     */
    @ElementCollection
    @CollectionTable(name = "audit_alert_notify_channels", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "channel")
    public List<String> notifyChannels = new ArrayList<>();

    /**
     * 是否启用
     */
    @Column(nullable = false)
    public Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}