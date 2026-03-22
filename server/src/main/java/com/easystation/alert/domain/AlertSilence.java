package com.easystation.alert.domain;

import com.easystation.alert.enums.AlertLevel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 告警静默规则配置
 */
@Entity
@Table(name = "alert_silence")
@Getter
@Setter
public class AlertSilence extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    public String description;

    /**
     * 静默匹配条件（JSON 格式）
     * 可包含：eventType、level、source、tags 等
     */
    @Column(columnDefinition = "TEXT")
    public String matchCondition;

    /**
     * 静默开始时间（可重复模式）
     */
    @Column
    public LocalDateTime silenceStart;

    /**
     * 静默结束时间
     */
    @Column
    public LocalDateTime silenceEnd;

    /**
     * 静默时长（秒），用于相对时间模式
     */
    @Column
    public Integer durationSeconds;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    public boolean enabled = true;

    /**
     * 创建者
     */
    @Column
    public String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}