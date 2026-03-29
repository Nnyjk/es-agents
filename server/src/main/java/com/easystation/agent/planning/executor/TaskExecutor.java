package com.easystation.agent.planning.executor;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskExecutionLog;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 任务执行器接口
 * 提供任务执行、状态更新、重试和异常处理能力
 */
public interface TaskExecutor {

    /**
     * 执行单个任务
     *
     * @param task 要执行的任务
     * @return 执行结果
     */
    ExecutionResult execute(PlanningTask task);

    /**
     * 执行指定 ID 的任务
     *
     * @param taskId 任务 ID
     * @return 执行结果
     */
    ExecutionResult executeById(UUID taskId);

    /**
     * 执行指定目标的下一个可执行任务
     *
     * @param goalId 目标 ID
     * @return 执行结果
     */
    Optional<ExecutionResult> executeNext(UUID goalId);

    /**
     * 执行下一个可执行任务（全局）
     *
     * @return 执行结果
     */
    Optional<ExecutionResult> executeNext();

    /**
     * 执行指定目标的任务序列
     * 按依赖顺序依次执行
     *
     * @param goalId 目标 ID
     * @return 执行结果列表
     */
    List<ExecutionResult> executeSequence(UUID goalId);

    /**
     * 执行指定任务列表（按顺序）
     *
     * @param tasks 任务列表
     * @return 执行结果列表
     */
    List<ExecutionResult> executeTasks(List<PlanningTask> tasks);

    /**
     * 开始任务执行
     * 更新状态为 RUNNING
     *
     * @param task 任务
     * @return 更新后的任务
     */
    PlanningTask startExecution(PlanningTask task);

    /**
     * 完成任务执行
     * 更新状态为 COMPLETED
     *
     * @param task 任务
     * @param result 执行结果内容
     * @return 更新后的任务
     */
    PlanningTask completeExecution(PlanningTask task, String result);

    /**
     * 失败任务执行
     * 更新状态为 FAILED
     *
     * @param task 任务
     * @param errorMessage 错误信息
     * @return 更新后的任务
     */
    PlanningTask failExecution(PlanningTask task, String errorMessage);

    /**
     * 取消任务执行
     * 更新状态为 CANCELLED
     *
     * @param task 任务
     * @return 更新后的任务
     */
    PlanningTask cancelExecution(PlanningTask task);

    /**
     * 重试失败的任务
     *
     * @param taskId 任务 ID
     * @return 执行结果
     */
    ExecutionResult retry(UUID taskId);

    /**
     * 检查是否可以重试
     *
     * @param task 任务
     * @return 是否可重试
     */
    boolean canRetry(PlanningTask task);

    /**
     * 获取任务的执行日志
     *
     * @param taskId 任务 ID
     * @return 执行日志列表
     */
    List<PlanningTaskExecutionLog> getExecutionLogs(UUID taskId);

    /**
     * 记录执行日志
     *
     * @param task 任务
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param message 日志消息
     * @return 创建的日志
     */
    PlanningTaskExecutionLog logExecution(PlanningTask task, PlanningTaskStatus fromStatus,
                                          PlanningTaskStatus toStatus, String message);

    /**
     * 获取执行统计信息
     *
     * @param goalId 目标 ID
     * @return 执行统计
     */
    ExecutionStatistics getStatistics(UUID goalId);

    /**
     * 注册执行器处理器
     *
     * @param executorType 执行器类型
     * @param handler 处理器
     */
    void registerHandler(String executorType, TaskHandler handler);

    /**
     * 获取注册的处理器
     *
     * @param executorType 执行器类型
     * @return 处理器
     */
    Optional<TaskHandler> getHandler(String executorType);

    /**
     * 执行结果
     */
    class ExecutionResult {
        private final UUID taskId;
        private final boolean success;
        private final String result;
        private final String errorMessage;
        private final long durationMillis;
        private final int retryAttempt;

        public ExecutionResult(UUID taskId, boolean success, String result, String errorMessage,
                               long durationMillis, int retryAttempt) {
            this.taskId = taskId;
            this.success = success;
            this.result = result;
            this.errorMessage = errorMessage;
            this.durationMillis = durationMillis;
            this.retryAttempt = retryAttempt;
        }

        public UUID getTaskId() {
            return taskId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public int getRetryAttempt() {
            return retryAttempt;
        }

        public static ExecutionResult success(UUID taskId, String result, long durationMillis) {
            return new ExecutionResult(taskId, true, result, null, durationMillis, 0);
        }

        public static ExecutionResult failure(UUID taskId, String errorMessage, long durationMillis) {
            return new ExecutionResult(taskId, false, null, errorMessage, durationMillis, 0);
        }

        public static ExecutionResult retry(UUID taskId, String errorMessage, long durationMillis, int retryAttempt) {
            return new ExecutionResult(taskId, false, null, errorMessage, durationMillis, retryAttempt);
        }

        @Override
        public String toString() {
            return "ExecutionResult{" +
                    "taskId=" + taskId +
                    ", success=" + success +
                    ", duration=" + durationMillis + "ms" +
                    ", retryAttempt=" + retryAttempt +
                    (success ? ", result='" + result + "'" : ", error='" + errorMessage + "'") +
                    '}';
        }
    }

    /**
     * 执行统计信息
     */
    class ExecutionStatistics {
        private final UUID goalId;
        private final int totalExecuted;
        private final int successCount;
        private final int failureCount;
        private final int retryCount;
        private final long totalDurationMillis;
        private final double averageDurationMillis;

        public ExecutionStatistics(UUID goalId, int totalExecuted, int successCount,
                                   int failureCount, int retryCount, long totalDurationMillis) {
            this.goalId = goalId;
            this.totalExecuted = totalExecuted;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.retryCount = retryCount;
            this.totalDurationMillis = totalDurationMillis;
            this.averageDurationMillis = totalExecuted > 0 ? totalDurationMillis / totalExecuted : 0;
        }

        public UUID getGoalId() {
            return goalId;
        }

        public int getTotalExecuted() {
            return totalExecuted;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public long getTotalDurationMillis() {
            return totalDurationMillis;
        }

        public double getAverageDurationMillis() {
            return averageDurationMillis;
        }

        public double getSuccessRate() {
            if (totalExecuted == 0) return 0;
            return (successCount * 100.0) / totalExecuted;
        }

        @Override
        public String toString() {
            return "ExecutionStatistics{" +
                    "goalId=" + goalId +
                    ", total=" + totalExecuted +
                    ", success=" + successCount +
                    ", failure=" + failureCount +
                    ", retries=" + retryCount +
                    ", totalDuration=" + totalDurationMillis + "ms" +
                    ", avgDuration=" + averageDurationMillis + "ms" +
                    ", successRate=" + String.format("%.1f%%", getSuccessRate()) +
                    '}';
        }
    }

    /**
     * 任务处理器接口
     * 用于处理特定类型的任务
     */
    interface TaskHandler {
        /**
         * 处理任务
         *
         * @param task 任务
         * @return 处理结果
         */
        HandlerResult handle(PlanningTask task);

        /**
         * 处理器名称
         */
        String getName();
    }

    /**
     * 处理器结果
     */
    class HandlerResult {
        private final boolean success;
        private final String result;
        private final String errorMessage;

        public HandlerResult(boolean success, String result, String errorMessage) {
            this.success = success;
            this.result = result;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static HandlerResult success(String result) {
            return new HandlerResult(true, result, null);
        }

        public static HandlerResult failure(String errorMessage) {
            return new HandlerResult(false, null, errorMessage);
        }
    }
}