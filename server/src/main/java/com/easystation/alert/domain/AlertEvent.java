package com.easystation.alert.domain;

import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 告警事件
 */
@Entity
@Table(name = "alert_event")
@Getter
@Setter
public class AlertEvent extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertStatus status = AlertStatus.PENDING;

    @Column(nullable = false)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String message;

    /**
     * 关联的资源 ID（如 Agent ID、Host ID）
     */
    public UUID resourceId;

    /**
     * 关联的资源类型
     */
    public String resourceType;

    /**
     * 关联的环境 ID
     */
    public UUID environmentId;

    /**
     * 触发告警的规则 ID
     */
    public UUID ruleId;

    /**
     * 告警次数
     */
    public int count = 1;

    /**
     * 最后通知时间
     */
    public LocalDateTime lastNotifiedAt;

    /**
     * 确认人
     */
    public String acknowledgedBy;

    /**
     * 确认时间
     */
    public LocalDateTime acknowledgedAt;

    /**
     * 解决人
     */
    public String resolvedBy;

    /**
     * 解决时间
     */
    public LocalDateTime resolvedAt;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}