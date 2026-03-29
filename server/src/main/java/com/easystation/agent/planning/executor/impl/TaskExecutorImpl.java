package com.easystation.agent.planning.executor.impl;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskExecutionLog;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.executor.TaskExecutor;
import com.easystation.agent.planning.repository.PlanningTaskExecutionLogRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import com.easystation.agent.planning.scheduler.TaskScheduler;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务执行器实现
 * 提供任务执行、状态更新、重试机制和异常处理
 */
@ApplicationScoped
public class TaskExecutorImpl implements TaskExecutor {

    /** 默认执行者标识 */
    private static final String DEFAULT_EXECUTOR = "TASK_EXECUTOR";

    /** 执行器处理器注册表 */
    private final Map<String, TaskHandler> handlers = new ConcurrentHashMap<>();

    @Inject
    PlanningTaskRepository taskRepository;

    @Inject
    PlanningTaskExecutionLogRepository logRepository;

    @Inject
    TaskScheduler taskScheduler;

    @Override
    @Transactional
    public ExecutionResult execute(PlanningTask task) {
        if (task == null || task.id == null) {
            Log.warn("Task is null or has no ID, cannot execute");
            return ExecutionResult.failure(null, "Task is null or has no ID", 0);
        }

        // 检查是否可以执行
        if (!taskScheduler.canExecute(task)) {
            Log.warnf("Task %s cannot be executed (status: %s, dependencies not met)",
                    task.id, task.status);
            return ExecutionResult.failure(task.id, "Task cannot be executed", 0);
        }

        long startTime = System.currentTimeMillis();
        int retryAttempt = task.retryCount != null ? task.retryCount : 0;

        try {
            // 开始执行
            PlanningTask runningTask = startExecution(task);

            // 执行任务
            HandlerResult handlerResult = doExecute(runningTask);

            long duration = System.currentTimeMillis() - startTime;

            if (handlerResult.isSuccess()) {
                // 完成执行
                completeExecution(runningTask, handlerResult.getResult());
                Log.infof("Task %s completed successfully in %dms", task.id, duration);
                return ExecutionResult.success(task.id, handlerResult.getResult(), duration);
            } else {
                // 失败处理
                failExecution(runningTask, handlerResult.getErrorMessage());
                Log.warnf("Task %s failed after %dms: %s", task.id, duration, handlerResult.getErrorMessage());
                return ExecutionResult.failure(task.id, handlerResult.getErrorMessage(), duration);
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String errorMessage = "Execution exception: " + e.getMessage();
            Log.errorf("Task %s execution error: %s", task.id, errorMessage);

            // 失败处理
            PlanningTask freshTask = taskRepository.findById(task.id);
            if (freshTask != null) {
                failExecution(freshTask, errorMessage);
            }

            return ExecutionResult.failure(task.id, errorMessage, duration);
        }
    }

    @Override
    @Transactional
    public ExecutionResult executeById(UUID taskId) {
        if (taskId == null) {
            return ExecutionResult.failure(null, "Task ID is null", 0);
        }

        PlanningTask task = taskRepository.findById(taskId);
        if (task == null) {
            Log.warnf("Task %s not found", taskId);
            return ExecutionResult.failure(taskId, "Task not found", 0);
        }

        return execute(task);
    }

    @Override
    @Transactional
    public Optional<ExecutionResult> executeNext(UUID goalId) {
        Optional<PlanningTask> nextTask = taskScheduler.getNextExecutableTask(goalId);

        if (nextTask.isEmpty()) {
            Log.debugf("No executable task found for goal: %s", goalId);
            return Optional.empty();
        }

        return Optional.of(execute(nextTask.get()));
    }

    @Override
    @Transactional
    public Optional<ExecutionResult> executeNext() {
        Optional<PlanningTask> nextTask = taskScheduler.getNextExecutableTask();

        if (nextTask.isEmpty()) {
            Log.debug("No executable task found globally");
            return Optional.empty();
        }

        return Optional.of(execute(nextTask.get()));
    }

    @Override
    @Transactional
    public List<ExecutionResult> executeSequence(UUID goalId) {
        if (goalId == null) {
            return Collections.emptyList();
        }

        Log.infof("Executing task sequence for goal: %s", goalId);

        List<ExecutionResult> results = new ArrayList<>();
        List<PlanningTask> executionOrder = taskScheduler.getExecutionOrder(goalId);

        for (PlanningTask task : executionOrder) {
            // 检查是否需要执行（状态为 SCHEDULED 或 RETRYING）
            if (task.status == PlanningTaskStatus.SCHEDULED
                    || task.status == PlanningTaskStatus.RETRYING) {

                ExecutionResult result = execute(task);
                results.add(result);

                // 如果失败且不可继续，停止执行
                if (!result.isSuccess() && !taskScheduler.areDependenciesMet(task.id)) {
                    Log.warnf("Stopping sequence execution due to failure of task %s", task.id);
                    break;
                }
            }
        }

        Log.infof("Sequence execution completed: %d tasks executed", results.size());
        return results;
    }

    @Override
    @Transactional
    public List<ExecutionResult> executeTasks(List<PlanningTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExecutionResult> results = new ArrayList<>();
        for (PlanningTask task : tasks) {
            results.add(execute(task));
        }

        return results;
    }

    @Override
    @Transactional
    public PlanningTask startExecution(PlanningTask task) {
        if (task == null) {
            return null;
        }

        PlanningTaskStatus fromStatus = task.status;
        task.status = PlanningTaskStatus.RUNNING;
        task.startedAt = LocalDateTime.now();

        taskRepository.persist(task);
        logExecution(task, fromStatus, PlanningTaskStatus.RUNNING, "Task execution started");

        Log.debugf("Task %s started execution", task.id);
        return task;
    }

    @Override
    @Transactional
    public PlanningTask completeExecution(PlanningTask task, String result) {
        if (task == null) {
            return null;
        }

        PlanningTaskStatus fromStatus = task.status;
        task.status = PlanningTaskStatus.COMPLETED;
        task.result = result;
        task.completedAt = LocalDateTime.now();

        // 计算实际执行时间
        if (task.startedAt != null) {
            long durationSeconds = java.time.Duration.between(task.startedAt, task.completedAt).getSeconds();
            task.actualDurationSeconds = durationSeconds;
        }

        taskRepository.persist(task);
        logExecution(task, fromStatus, PlanningTaskStatus.COMPLETED, "Task completed: " + truncate(result, 100));

        Log.infof("Task %s completed", task.id);
        return task;
    }

    @Override
    @Transactional
    public PlanningTask failExecution(PlanningTask task, String errorMessage) {
        if (task == null) {
            return null;
        }

        PlanningTaskStatus fromStatus = task.status;
        task.status = PlanningTaskStatus.FAILED;
        task.errorMessage = errorMessage;
        task.completedAt = LocalDateTime.now();

        // 计算实际执行时间
        if (task.startedAt != null) {
            long durationSeconds = java.time.Duration.between(task.startedAt, task.completedAt).getSeconds();
            task.actualDurationSeconds = durationSeconds;
        }

        taskRepository.persist(task);
        logExecution(task, fromStatus, PlanningTaskStatus.FAILED, "Task failed: " + truncate(errorMessage, 100));

        Log.warnf("Task %s failed: %s", task.id, errorMessage);
        return task;
    }

    @Override
    @Transactional
    public PlanningTask cancelExecution(PlanningTask task) {
        if (task == null) {
            return null;
        }

        PlanningTaskStatus fromStatus = task.status;
        task.status = PlanningTaskStatus.CANCELLED;
        task.completedAt = LocalDateTime.now();

        taskRepository.persist(task);
        logExecution(task, fromStatus, PlanningTaskStatus.CANCELLED, "Task cancelled");

        Log.infof("Task %s cancelled", task.id);
        return task;
    }

    @Override
    @Transactional
    public ExecutionResult retry(UUID taskId) {
        if (taskId == null) {
            return ExecutionResult.failure(null, "Task ID is null", 0);
        }

        PlanningTask task = taskRepository.findById(taskId);
        if (task == null) {
            return ExecutionResult.failure(taskId, "Task not found", 0);
        }

        if (!canRetry(task)) {
            return ExecutionResult.failure(taskId, "Task cannot be retried", 0);
        }

        // 重新调度失败任务
        boolean rescheduled = taskScheduler.rescheduleFailedTask(taskId);
        if (!rescheduled) {
            return ExecutionResult.failure(taskId, "Failed to reschedule task", 0);
        }

        // 重新获取任务（状态已更新）
        task = taskRepository.findById(taskId);

        Log.infof("Retrying task %s (attempt %d)", taskId, task.retryCount);

        // 执行任务
        ExecutionResult result = execute(task);

        return ExecutionResult.retry(taskId, result.getErrorMessage(),
                result.getDurationMillis(), task.retryCount);
    }

    @Override
    public boolean canRetry(PlanningTask task) {
        if (task == null) {
            return false;
        }
        return task.canRetry();
    }

    @Override
    public List<PlanningTaskExecutionLog> getExecutionLogs(UUID taskId) {
        if (taskId == null) {
            return Collections.emptyList();
        }
        return logRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Override
    @Transactional
    public PlanningTaskExecutionLog logExecution(PlanningTask task, PlanningTaskStatus fromStatus,
                                                 PlanningTaskStatus toStatus, String message) {
        if (task == null) {
            return null;
        }

        PlanningTaskExecutionLog log = PlanningTaskExecutionLog.create(
                task, fromStatus, toStatus, message, DEFAULT_EXECUTOR);
        logRepository.persist(log);

        Log.debugf("Logged execution: %s -> %s for task %s", fromStatus, toStatus, task.id);
        return log;
    }

    @Override
    public ExecutionStatistics getStatistics(UUID goalId) {
        if (goalId == null) {
            return new ExecutionStatistics(null, 0, 0, 0, 0, 0);
        }

        List<PlanningTask> tasks = taskRepository.findByGoalId(goalId);

        int total = tasks.size();
        int successCount = 0;
        int failureCount = 0;
        int retryCount = 0;
        long totalDuration = 0;

        for (PlanningTask task : tasks) {
            if (task.status == PlanningTaskStatus.COMPLETED) {
                successCount++;
                if (task.actualDurationSeconds != null) {
                    totalDuration += task.actualDurationSeconds * 1000;
                }
            } else if (task.status == PlanningTaskStatus.FAILED) {
                failureCount++;
                if (task.retryCount != null && task.retryCount > 0) {
                    retryCount += task.retryCount;
                }
            }
        }

        return new ExecutionStatistics(goalId, successCount + failureCount, successCount,
                failureCount, retryCount, totalDuration);
    }

    @Override
    public void registerHandler(String executorType, TaskHandler handler) {
        if (executorType == null || handler == null) {
            Log.warn("Cannot register handler with null executorType or handler");
            return;
        }

        handlers.put(executorType, handler);
        Log.infof("Registered handler for executor type: %s", executorType);
    }

    @Override
    public Optional<TaskHandler> getHandler(String executorType) {
        if (executorType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlers.get(executorType));
    }

    /**
     * 执行任务的实际逻辑
     */
    private HandlerResult doExecute(PlanningTask task) {
        // 检查是否有注册的处理器
        String executorType = task.executorType != null ? task.executorType : "DEFAULT";

        Optional<TaskHandler> handlerOpt = getHandler(executorType);
        if (handlerOpt.isPresent()) {
            Log.debugf("Using registered handler for executor type: %s", executorType);
            return handlerOpt.get().handle(task);
        }

        // 默认处理逻辑
        return defaultHandle(task);
    }

    /**
     * 默认任务处理逻辑
     */
    private HandlerResult defaultHandle(PlanningTask task) {
        Log.debugf("Executing task %s with default handler", task.id);

        // 对于根任务，标记为成功
        if ("ROOT".equals(task.executorType)) {
            return HandlerResult.success("Root task completed - dependencies processed");
        }

        // 对于其他任务，简单模拟执行
        String description = task.description != null ? task.description : "Unknown task";

        // 模拟执行时间（实际应该有真实逻辑）
        try {
            Thread.sleep(Math.min(100, task.estimatedDurationSeconds != null
                    ? task.estimatedDurationSeconds * 10 : 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 返回成功结果
        return HandlerResult.success("Executed: " + truncate(description, 200));
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}