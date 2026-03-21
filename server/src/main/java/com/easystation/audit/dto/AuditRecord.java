package com.easystation.audit.dto;

import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
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
}