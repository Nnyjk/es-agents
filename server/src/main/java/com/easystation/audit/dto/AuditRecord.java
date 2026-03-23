package com.easystation.audit.dto;

import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AuditRecord {

    public record Create(
            @NotBlank String username,
            UUID userId,
            @NotNull AuditAction action,
            @NotNull AuditResult result,
            @NotBlank String description,
            String resourceType,
            UUID resourceId,
            String details,
            String requestParams,
            String responseResult,
            String clientIp,
            String userAgent,
            String requestPath,
            String requestMethod,
            Long duration,
            String errorMessage
    ) {}

    public record Query(
            String username,
            UUID userId,
            AuditAction action,
            AuditResult result,
            String resourceType,
            UUID resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String keyword,
            Integer limit,
            Integer offset
    ) {}

    public record Detail(
            UUID id,
            String username,
            UUID userId,
            AuditAction action,
            AuditResult result,
            String description,
            String resourceType,
            UUID resourceId,
            String details,
            String requestParams,
            String responseResult,
            String clientIp,
            String userAgent,
            String requestPath,
            String requestMethod,
            Long duration,
            String errorMessage,
            LocalDateTime createdAt
    ) {}

    public record Summary(
            long total,
            long successCount,
            long failedCount,
            long todayCount
    ) {}

    // ==================== 导出相关 ====================

    public record ExportRequest(
            String username,
            UUID userId,
            AuditAction action,
            AuditResult result,
            String resourceType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String keyword,
            String format // json, csv, excel
    ) {}

    public record ExportResult(
            String downloadUrl,
            String filename,
            Integer recordCount,
            Long fileSize
    ) {}

    // ==================== 统计相关 ====================

    public record StatisticsByUser(
            String username,
            UUID userId,
            Long totalOperations,
            Long successCount,
            Long failedCount,
            Long failedRate
    ) {}

    public record StatisticsByAction(
            AuditAction action,
            Long totalCount,
            Long successCount,
            Long failedCount
    ) {}

    public record StatisticsByDate(
            LocalDateTime date,
            Long totalCount,
            Long successCount,
            Long failedCount
    ) {}

    public record StatisticsByHour(
            Integer hour,
            Long totalCount,
            Long successCount,
            Long failedCount
    ) {}

    public record StatisticsSummary(
            Long totalOperations,
            Long successCount,
            Long failedCount,
            Double successRate,
            Long uniqueUsers,
            Long uniqueResources
    ) {}

    // ==================== 归档相关 ====================

    public record ArchiveRequest(
            LocalDateTime beforeDate,
            Boolean includeSuccess,
            Boolean includeFailed
    ) {}

    public record ArchiveResult(
            Integer archivedCount,
            String archiveFile,
            Long archiveSize
    ) {}

    public record ArchiveInfo(
            String archiveId,
            String filename,
            Long fileSize,
            Integer recordCount,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime archivedAt
    ) {}

    // ==================== 清理相关 ====================

    public record CleanupRequest(
            LocalDateTime beforeDate,
            Boolean dryRun
    ) {}

    public record CleanupResult(
            Integer deletedCount,
            LocalDateTime cleanedBefore
    ) {}

    // ==================== 告警配置相关 ====================

    public record AlertConfigCreate(
            String name,
            String description,
            String alertType, // SENSITIVE_OPERATION, FAILED_OPERATION, ABNORMAL_IP, FREQUENT_ACCESS
            List<AuditAction> sensitiveActions,
            List<String> whitelistUsers,
            Integer failureThreshold,
            Integer timeWindowMinutes,
            List<String> notifyChannels,
            Boolean enabled
    ) {}

    public record AlertConfigUpdate(
            String name,
            String description,
            List<AuditAction> sensitiveActions,
            List<String> whitelistUsers,
            Integer failureThreshold,
            Integer timeWindowMinutes,
            List<String> notifyChannels,
            Boolean enabled
    ) {}

    public record AlertConfigDetail(
            UUID id,
            String name,
            String description,
            String alertType,
            List<AuditAction> sensitiveActions,
            List<String> whitelistUsers,
            Integer failureThreshold,
            Integer timeWindowMinutes,
            List<String> notifyChannels,
            Boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record AlertRecord(
            UUID id,
            UUID configId,
            String alertType,
            String username,
            AuditAction action,
            String details,
            LocalDateTime triggeredAt,
            String status // PENDING, NOTIFIED, RESOLVED
    ) {}
}