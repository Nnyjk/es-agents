package com.easystation.agent.planning.dto;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;

import java.util.UUID;

/**
 * 执行结果响应
 */
public record ExecutionResultResponse(
    UUID taskId,
    boolean success,
    String result,
    String errorMessage,
    long durationMillis,
    PlanningTaskStatus finalStatus,
    int retryAttempt
) {
    /**
     * 成功执行响应
     */
    public static ExecutionResultResponse success(UUID taskId, String result, long durationMillis) {
        return new ExecutionResultResponse(
            taskId,
            true,
            result,
            null,
            durationMillis,
            PlanningTaskStatus.COMPLETED,
            0
        );
    }

    /**
     * 失败执行响应
     */
    public static ExecutionResultResponse failure(UUID taskId, String errorMessage, long durationMillis) {
        return new ExecutionResultResponse(
            taskId,
            false,
            null,
            errorMessage,
            durationMillis,
            PlanningTaskStatus.FAILED,
            0
        );
    }

    /**
     * 重试响应
     */
    public static ExecutionResultResponse retry(UUID taskId, String errorMessage, long durationMillis, int retryAttempt) {
        return new ExecutionResultResponse(
            taskId,
            false,
            null,
            errorMessage,
            durationMillis,
            PlanningTaskStatus.RETRYING,
            retryAttempt
        );
    }
}