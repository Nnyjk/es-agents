package com.easystation.agent.planning.dto;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;

import java.util.List;
import java.util.UUID;

/**
 * 任务列表响应
 */
public record TaskListResponse(
    List<TaskRecord> tasks,
    int total,
    int page,
    int pageSize,
    TaskStatistics statistics
) {
    /**
     * 任务统计信息
     */
    public record TaskStatistics(
        int createdCount,
        int readyCount,
        int scheduledCount,
        int runningCount,
        int completedCount,
        int failedCount,
        int cancelledCount,
        double completionRate
    ) {}

    /**
     * 简化的列表响应
     */
    public static TaskListResponse of(List<TaskRecord> tasks) {
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.COMPLETED).count();
        int failed = (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.FAILED).count();

        return new TaskListResponse(
            tasks,
            total,
            1,
            total,
            new TaskStatistics(
                (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.CREATED).count(),
                (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.READY).count(),
                (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.SCHEDULED).count(),
                (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.RUNNING).count(),
                completed,
                failed,
                (int) tasks.stream().filter(t -> t.status() == PlanningTaskStatus.CANCELLED).count(),
                total > 0 ? (completed + failed) * 100.0 / total : 0
            )
        );
    }

    /**
     * 空列表响应
     */
    public static TaskListResponse empty() {
        return new TaskListResponse(
            List.of(),
            0,
            1,
            50,
            new TaskStatistics(0, 0, 0, 0, 0, 0, 0, 0)
        );
    }
}