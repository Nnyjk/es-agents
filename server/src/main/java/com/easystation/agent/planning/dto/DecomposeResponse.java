package com.easystation.agent.planning.dto;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;

import java.util.List;
import java.util.UUID;

/**
 * 任务分解响应
 */
public record DecomposeResponse(
    UUID goalId,
    String goal,
    int totalTasks,
    int maxDepth,
    List<TaskRecord> tasks,
    List<DependencyInfo> dependencies,
    ExecutionPlan executionPlan,
    boolean valid,
    String message
) {
    /**
     * 依赖信息
     */
    public record DependencyInfo(
        UUID taskId,
        UUID dependsOnTaskId,
        String dependencyType
    ) {}

    /**
     * 执行计划
     */
    public record ExecutionPlan(
        List<UUID> executionOrder,
        int estimatedTotalDurationSeconds,
        int parallelizableTasks
    ) {}

    /**
     * 简化的分解响应
     */
    public static DecomposeResponse simple(UUID goalId, String goal, List<TaskRecord> tasks) {
        return new DecomposeResponse(
            goalId,
            goal,
            tasks.size(),
            3,
            tasks,
            List.of(),
            new ExecutionPlan(List.of(), 0, 0),
            true,
            "任务分解成功"
        );
    }

    /**
     * 分解失败响应
     */
    public static DecomposeResponse failure(String goal, String message) {
        return new DecomposeResponse(
            null,
            goal,
            0,
            0,
            List.of(),
            List.of(),
            null,
            false,
            message
        );
    }
}