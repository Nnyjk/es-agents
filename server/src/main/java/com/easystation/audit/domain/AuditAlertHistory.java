package com.easystation.audit.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 审计告警历史
 */
@Entity
@Table(name = "audit_alert_history")
@Getter
@Setter
public class AuditAlertHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 告警配置ID
     */
    @Column(name = "config_id")
    public UUID configId;

    /**
     * 告警名称
     */
    @Column(nullable = false)
    public String alertName;

    /**
     * 告警类型
     */
    @Column(nullable = false)
    public String alertType;

    /**
     * 触发用户
     */
    public String triggerUser;

    /**
     * 触发IP
     */
    public String triggerIp;

    /**
     * 告警详情
     */
    @Column(columnDefinition = "TEXT")
    public String detail;

    /**
     * 相关审计记录ID列表
     */
    @ElementCollection
    @CollectionTable(name = "audit_alert_related_records", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "record_id")
    public List<UUID> relatedRecordIds = new ArrayList<>();

    /**
     * 通知渠道
     */
    public String notifyChannel;

    /**
     * 通知状态: PENDING, SENT, FAILED
     */
    public String notifyStatus;

    /**
     * 通知错误信息
     */
    @Column(length = 1000)
    public String notifyError;

    /**
     * 告警状态: ACTIVE, ACKNOWLEDGED, RESOLVED
     */
    @Column(nullable = false)
    public String status = "ACTIVE";

    /**
     * 确认人
     */
    public String acknowledgedBy;

    /**
     * 确认时间
     */
    public LocalDateTime acknowledgedAt;

    /**
     * 备注
     */
    @Column(length = 500)
    public String remark;

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