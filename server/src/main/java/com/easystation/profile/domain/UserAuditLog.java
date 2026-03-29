package com.easystation.profile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户审计日志实体
 * 
 * 增强功能：
 * - 敏感操作标记
 * - 防篡改签名
 */
@Entity
@Table(name = "user_audit_log", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_audit_sensitive", columnList = "is_sensitive"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@Setter
public class UserAuditLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, length = 100)
    public String action;

    @Column(name = "resource_type", length = 100)
    public String resourceType;

    @Column(name = "resource_id")
    public String resourceId;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent", length = 500)
    public String userAgent;

    @Column(nullable = false, length = 20)
    public String status; // success, failure

    @Column(name = "request_data", columnDefinition = "TEXT")
    public String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    public String responseData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    public LocalDateTime createdAt;

    // ========== 新增字段：敏感操作与防篡改 ==========
    
    /**
     * 是否为敏感操作
     * 敏感操作包括：删除、权限变更、配置修改、数据导出等
     */
    @Column(name = "is_sensitive", nullable = false)
    public boolean isSensitive = false;

    /**
     * 操作风险等级：LOW, MEDIUM, HIGH, CRITICAL
     */
    @Column(name = "risk_level", length = 20)
    public String riskLevel = "LOW";

    /**
     * 防篡改签名 - 基于日志内容生成的 HMAC-SHA256 签名
     * 用于验证日志是否被篡改
     */
    @Column(name = "integrity_signature", length = 128)
    public String integritySignature;

    /**
     * 日志内容哈希 - 用于快速校验
     */
    @Column(name = "content_hash", length = 64)
    public String contentHash;

    /**
     * 关联的会话 ID
     */
    @Column(name = "session_id")
    public String sessionId;

    /**
     * 操作分类：AUTH, DATA, CONFIG, SYSTEM, EXPORT
     */
    @Column(name = "operation_category", length = 20)
    public String operationCategory;

    /**
     * 是否需要人工审查（敏感操作自动标记）
     */
    @Column(name = "requires_review")
    public Boolean requiresReview = false;

    /**
     * 审查状态：PENDING, REVIEWED, FLAGGED
     */
    @Column(name = "review_status", length = 20)
    public String reviewStatus = "PENDING";

    /**
     * 审查备注
     */
    @Column(name = "review_notes", columnDefinition = "TEXT")
    public String reviewNotes;

    /**
     * 审查人 ID
     */
    @Column(name = "reviewer_id")
    public UUID reviewerId;

    /**
     * 审查时间
     */
    @Column(name = "reviewed_at")
    public LocalDateTime reviewedAt;

    // ========== 敏感操作判定 ==========
    
    /**
     * 判断操作是否为敏感操作
     */
    public static boolean isSensitiveAction(String action, String resourceType) {
        if (action == null || resourceType == null) {
            return false;
        }
        
        // 删除操作都是敏感的
        if (action.equalsIgnoreCase("DELETE") || action.equalsIgnoreCase("REMOVE")) {
            return true;
        }
        
        // 权限相关操作
        if (resourceType.equalsIgnoreCase("PERMISSION") || 
            resourceType.equalsIgnoreCase("ROLE") ||
            resourceType.equalsIgnoreCase("USER_ROLE")) {
            return true;
        }
        
        // 配置修改
        if (resourceType.equalsIgnoreCase("CONFIG") || 
            resourceType.equalsIgnoreCase("SETTING")) {
            return true;
        }
        
        // 数据导出
        if (action.equalsIgnoreCase("EXPORT") || action.equalsIgnoreCase("DOWNLOAD")) {
            return true;
        }
        
        // 批量操作
        if (action.equalsIgnoreCase("BATCH_DELETE") || action.equalsIgnoreCase("BATCH_UPDATE")) {
            return true;
        }
        
        return false;
    }

    /**
     * 计算风险等级
     */
    public static String calculateRiskLevel(String action, String resourceType, boolean isSensitive) {
        if (!isSensitive) {
            return "LOW";
        }
        
        // CRITICAL: 删除用户、删除角色、权限批量修改
        if ((action.equalsIgnoreCase("DELETE") && 
             (resourceType.equalsIgnoreCase("USER") || resourceType.equalsIgnoreCase("ROLE"))) ||
            action.equalsIgnoreCase("BATCH_UPDATE") && resourceType.equalsIgnoreCase("PERMISSION")) {
            return "CRITICAL";
        }
        
        // HIGH: 权限修改、配置修改、数据导出
        if (resourceType.equalsIgnoreCase("PERMISSION") ||
            resourceType.equalsIgnoreCase("CONFIG") ||
            action.equalsIgnoreCase("EXPORT")) {
            return "HIGH";
        }
        
        // MEDIUM: 其他敏感操作
        return "MEDIUM";
    }
}
