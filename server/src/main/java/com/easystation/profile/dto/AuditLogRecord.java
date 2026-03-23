package com.easystation.profile.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AuditLogRecord(
    UUID id,
    String action,
    String resourceType,
    String resourceId,
    String description,
    String ipAddress,
    String userAgent,
    String status,
    String errorMessage,
    Long durationMs,
    LocalDateTime createdAt
) {
    public record Query(
        String keyword,
        String action,
        String resourceType,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer limit,
        Integer offset
    ) {}

    public record Summary(
        long totalCount,
        long successCount,
        long failedCount,
        List<ActionCount> topActions
    ) {}

    public record ActionCount(
        String action,
        long count
    ) {}
}