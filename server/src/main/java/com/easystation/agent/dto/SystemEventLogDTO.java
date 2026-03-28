package com.easystation.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 系统事件日志 DTO
 */
public record SystemEventLogDTO(
    Long id,
    @NotBlank String eventType,
    @NotNull String eventLevel,
    String module,
    String operation,
    String targetType,
    Long targetId,
    Long userId,
    @NotBlank String message,
    String details,
    String clientIp,
    String requestPath,
    Long duration,
    String errorMessage,
    LocalDateTime createdAt
) {
    /**
     * 事件查询条件
     */
    public record EventQueryCriteria(
        String eventType,
        String eventLevel,
        String module,
        String operation,
        String targetType,
        Long targetId,
        Long userId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String keyword
    ) {}

    /**
     * 分页响应
     */
    public record EventLogPage(
        java.util.List<SystemEventLogDTO> logs,
        long total,
        int page,
        int size,
        int totalPages
    ) {}
}
