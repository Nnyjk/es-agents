package com.easystation.alert.domain;

import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 告警规则配置
 */
@Entity(name = "AlertRuleEntity")
@Table(name = "alert_rule")
@Getter
@Setter
public class AlertRule extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertLevel level;

    /**
     * 触发条件（JSON 格式）
     * 例如：{"threshold": 3, "duration": 60} 表示 60 秒内触发 3 次
     */
    @Column(columnDefinition = "TEXT")
    public String condition;

    /**
     * 关联的环境 ID 列表（JSON 数组）
     * 为空表示所有环境
     */
    @Column(columnDefinition = "TEXT")
    public String environmentIds;

    /**
     * 关联的渠道 ID 列表
     */
    @ElementCollection
    @CollectionTable(name = "alert_rule_channels", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "channel_id")
    public List<UUID> channelIds = new ArrayList<>();

    public boolean enabled = true;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}