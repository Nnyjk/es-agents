package com.easystation.agent.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统事件日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "system_event_log")
public class SystemEventLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * 事件类型
     */
    @Column(name = "event_type", nullable = false, length = 50)
    public String eventType;

    /**
     * 事件级别：INFO, WARN, ERROR
     */
    @Column(name = "event_level", nullable = false, length = 20)
    public String eventLevel;

    /**
     * 模块名称
     */
    @Column(name = "module", length = 50)
    public String module;

    /**
     * 操作类型
     */
    @Column(name = "operation", length = 50)
    public String operation;

    /**
     * 目标类型
     */
    @Column(name = "target_type", length = 50)
    public String targetType;

    /**
     * 目标 ID
     */
    @Column(name = "target_id")
    public Long targetId;

    /**
     * 用户 ID
     */
    @Column(name = "user_id")
    public Long userId;

    /**
     * 事件消息
     */
    @Column(name = "message", columnDefinition = "TEXT")
    public String message;

    /**
     * 详细信息（JSON）
     */
    @Column(name = "details", columnDefinition = "TEXT")
    public String details;

    /**
     * 客户端 IP
     */
    @Column(name = "client_ip", length = 50)
    public String clientIp;

    /**
     * 请求路径
     */
    @Column(name = "request_path", length = 500)
    public String requestPath;

    /**
     * 操作耗时（毫秒）
     */
    @Column(name = "duration")
    public Long duration;

    /**
     * 错误消息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
