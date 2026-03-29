package com.easystation.agent.planning.dto;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 任务规划数据传输对象
 */
public record TaskRecord(
    UUID id,
    UUID goalId,
    String description,
    PlanningTaskStatus status,
    TaskPriority priority,
    Integer priorityValue,
    UUID parentTaskId,
    Integer depth,
    Long estimatedDurationSeconds,
    Long actualDurationSeconds,
    String parameters,
    String result,
    String errorMessage,
    Integer retryCount,
    Integer maxRetryCount,
    String executorType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    List<UUID> dependencies
) {
    /**
     * 创建任务请求
     */
    public record Create(
        UUID goalId,
        String description,
        TaskPriority priority,
        String parameters,
        String executorType,
        Integer maxRetryCount
    ) {}

    /**
     * 更新任务请求
     */
    public record Update(
        String description,
        TaskPriority priority,
        String parameters,
        String executorType,
        Integer maxRetryCount
    ) {}

    /**
     * 更新状态请求
     */
    public record UpdateStatusRequest(
        PlanningTaskStatus status,
        String result,
        String errorMessage
    ) {}

    /**
     * 任务状态统计
     */
    public record TaskCounts(
        long created,
        long ready,
        long scheduled,
        long running,
        long completed,
        long failed,
        long cancelled
    ) {}
}