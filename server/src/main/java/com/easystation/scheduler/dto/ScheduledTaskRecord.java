package com.easystation.scheduler.dto;

import com.easystation.scheduler.enums.ExecutionStatus;
import com.easystation.scheduler.enums.TaskStatus;
import com.easystation.scheduler.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ScheduledTaskRecord {

    public record Create(
            @NotBlank String name,
            @NotNull TaskType type,
            String description,
            @NotBlank String cronExpression,
            String config,
            UUID targetId,
            String targetType,
            Integer maxRetries,
            Integer timeoutSeconds,
            Boolean alertOnFailure,
            String createdBy
    ) {}

    public record Update(
            String name,
            String description,
            String cronExpression,
            TaskStatus status,
            String config,
            UUID targetId,
            String targetType,
            Integer maxRetries,
            Integer timeoutSeconds,
            Boolean alertOnFailure
    ) {}

    public record Detail(
            UUID id,
            String name,
            TaskType type,
            String description,
            String cronExpression,
            TaskStatus status,
            String config,
            UUID targetId,
            String targetType,
            Integer maxRetries,
            Integer timeoutSeconds,
            boolean alertOnFailure,
            LocalDateTime lastExecutionAt,
            String lastExecutionStatus,
            LocalDateTime nextExecutionAt,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record Query(
            String keyword,
            TaskType type,
            TaskStatus status,
            Integer limit,
            Integer offset
    ) {}

    public record ExecuteRequest(
            String triggeredBy
    ) {}

    public record ExecutionDetail(
            UUID id,
            UUID taskId,
            String taskName,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Long durationMs,
            LocalDateTime scheduledAt,
            String triggerType,
            String triggeredBy,
            String result,
            String logs,
            String errorMessage,
            Integer retryCount,
            LocalDateTime createdAt
    ) {}

    public record ExecutionQuery(
            UUID taskId,
            ExecutionStatus status,
            String triggerType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit,
            Integer offset
    ) {}

    public record CronValidation(
            boolean valid,
            String message,
            LocalDateTime nextExecutionTime
    ) {}

    public record TaskStats(
            long totalTasks,
            long enabledTasks,
            long disabledTasks,
            long totalExecutions,
            long successExecutions,
            long failedExecutions
    ) {}
}