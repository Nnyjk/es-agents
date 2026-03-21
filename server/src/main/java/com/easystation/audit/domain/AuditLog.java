package com.easystation.audit.domain;

import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 审计日志
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 操作人用户名
     */
    @Column(nullable = false)
    public String username;

    /**
     * 操作人 ID
     */
    public UUID userId;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AuditAction action;

    /**
     * 操作结果
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AuditResult result;

    /**
     * 操作描述
     */
    @Column(nullable = false)
    public String description;

    /**
     * 操作资源类型
     */
    public String resourceType;

    /**
     * 操作资源 ID
     */
    public UUID resourceId;

    /**
     * 操作详情（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    public String details;

    /**
     * 请求参数（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    public String requestParams;

    /**
     * 响应结果（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    public String responseResult;

    /**
     * 客户端 IP 地址
     */
    public String clientIp;

    /**
     * User-Agent
     */
    public String userAgent;

    /**
     * 请求路径
     */
    public String requestPath;

    /**
     * 请求方法
     */
    public String requestMethod;

    /**
     * 执行时长（毫秒）
     */
    public Long duration;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    /**
     * 操作时间（不可修改）
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;
}