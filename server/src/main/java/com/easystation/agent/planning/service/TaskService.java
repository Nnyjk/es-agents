package com.easystation.agent.planning.service;

import com.easystation.agent.planning.domain.PlanningTask;
import com.easystation.agent.planning.domain.PlanningTaskDependency;
import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.dto.*;
import com.easystation.agent.planning.engine.TaskDecompositionEngine;
import com.easystation.agent.planning.executor.TaskExecutor;
import com.easystation.agent.planning.repository.PlanningTaskDependencyRepository;
import com.easystation.agent.planning.repository.PlanningTaskRepository;
import com.easystation.agent.planning.scheduler.TaskScheduler;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务规划服务
 * 提供任务分解、调度、执行和管理的统一接口
 */
@ApplicationScoped
public class TaskService {

    @Inject
    PlanningTaskRepository taskRepository;

    @Inject
    PlanningTaskDependencyRepository dependencyRepository;

    @Inject
    TaskDecompositionEngine decompositionEngine;

    @Inject
    TaskScheduler scheduler;

    @Inject
    TaskExecutor executor;

    /**
     * 分解目标为任务
     */
    @Transactional
    public DecomposeResponse decompose(DecomposeRequest request) {
        Log.infof("Decomposing goal: %s (maxDepth=%d)", request.goal(), request.maxDepth());

        try {
            List<PlanningTask> tasks = decompositionEngine.decompose(request.goal(), request.maxDepth());

            if (tasks.isEmpty()) {
                return DecomposeResponse.failure(request.goal(), "任务分解失败，未生成任何任务");
            }

            // 验证分解结果
            TaskDecompositionEngine.DecompositionResult validation = decompositionEngine.validate(tasks);
            if (!validation.isValid()) {
                Log.warnf("Decomposition validation failed: %s", validation.getMessage());
                return DecomposeResponse.failure(request.goal(), validation.getMessage());
            }

            // 获取执行顺序
            List<PlanningTask> executionOrder = decompositionEngine.getExecutionOrder(tasks);

            // 构建响应
            UUID goalId = tasks.get(0).goalId;
            List<TaskRecord> taskRecords = tasks.stream()
                    .map(this::toTaskRecord)
                    .collect(Collectors.toList());

            List<DecomposeResponse.DependencyInfo> dependencies = new ArrayList<>();
            for (PlanningTask task : tasks) {
                List<PlanningTaskDependency> deps = dependencyRepository.findByTaskId(task.id);
                for (PlanningTaskDependency dep : deps) {
                    dependencies.add(new DecomposeResponse.DependencyInfo(
                            dep.task.id,
                            dep.dependsOnTask.id,
                            dep.dependencyType
                    ));
                }
            }

            List<UUID> executionOrderIds = executionOrder.stream()
                    .map(t -> t.id)
                    .collect(Collectors.toList());

            int totalDuration = tasks.stream()
                    .filter(t -> t.estimatedDurationSeconds != null)
                    .mapToInt(t -> t.estimatedDurationSeconds.intValue())
                    .sum();

            // 计算可并行化的任务数（无依赖的任务）
            int parallelizable = (int) tasks.stream()
                    .filter(t -> dependencyRepository.countByTaskId(t.id) == 0)
                    .count();

            return new DecomposeResponse(
                    goalId,
                    request.goal(),
                    tasks.size(),
                    request.maxDepth(),
                    taskRecords,
                    dependencies,
                    new DecomposeResponse.ExecutionPlan(executionOrderIds, totalDuration, parallelizable),
                    true,
                    "任务分解成功，生成 " + tasks.size() + " 个任务"
            );
        } catch (Exception e) {
            Log.errorf("Decomposition failed: %s", e.getMessage());
            return DecomposeResponse.failure(request.goal(), "分解失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    public TaskRecord getById(UUID id) {
        PlanningTask task = taskRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("任务不存在: " + id));
        return toTaskRecord(task);
    }

    /**
     * 更新任务状态
     */
    @Transactional
    public TaskRecord updateStatus(UUID id, TaskRecord.UpdateStatusRequest request) {
        PlanningTask task = taskRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("任务不存在: " + id));

        PlanningTaskStatus oldStatus = task.status;
        task.status = request.status();

        if (request.result() != null) {
            task.result = request.result();
        }

        if (request.errorMessage() != null) {
            task.errorMessage = request.errorMessage();
        }

        // 根据状态更新时间
        if (request.status() == PlanningTaskStatus.RUNNING) {
            task.startedAt = LocalDateTime.now();
        } else if (request.status() == PlanningTaskStatus.COMPLETED ||
                   request.status() == PlanningTaskStatus.FAILED ||
                   request.status() == PlanningTaskStatus.CANCELLED) {
            task.completedAt = LocalDateTime.now();
            if (task.startedAt != null) {
                task.actualDurationSeconds = java.time.Duration.between(task.startedAt, task.completedAt).getSeconds();
            }
        }

        taskRepository.persist(task);

        Log.infof("Task %s status updated: %s -> %s", id, oldStatus, request.status());
        return toTaskRecord(task);
    }

    /**
     * 执行任务
     */
    @Transactional
    public ExecutionResultResponse execute(UUID id) {
        PlanningTask task = taskRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("任务不存在: " + id));

        if (!task.canExecute()) {
            return ExecutionResultResponse.failure(id,
                    "任务无法执行，当前状态: " + task.status, 0);
        }

        TaskExecutor.ExecutionResult result = executor.executeById(id);

        if (result.isSuccess()) {
            return ExecutionResultResponse.success(id, result.getResult(), result.getDurationMillis());
        } else {
            if (result.getRetryAttempt() > 0) {
                return ExecutionResultResponse.retry(id, result.getErrorMessage(),
                        result.getDurationMillis(), result.getRetryAttempt());
            }
            return ExecutionResultResponse.failure(id, result.getErrorMessage(), result.getDurationMillis());
        }
    }

    /**
     * 取消任务
     */
    @Transactional
    public TaskRecord cancel(UUID id) {
        PlanningTask task = taskRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("任务不存在: " + id));

        if (!task.canCancel()) {
            throw new IllegalStateException("任务无法取消，当前状态: " + task.status);
        }

        task = executor.cancelExecution(task);
        return toTaskRecord(task);
    }

    /**
     * 查询任务列表
     */
    public TaskListResponse list(PlanningTaskStatus status, int limit) {
        List<PlanningTask> tasks;
        if (status != null) {
            tasks = taskRepository.findByStatusOrderByPriority(status);
        } else {
            tasks = taskRepository.findAll().stream().limit(limit).collect(Collectors.toList());
        }

        List<TaskRecord> records = tasks.stream()
                .map(this::toTaskRecord)
                .collect(Collectors.toList());

        return TaskListResponse.of(records);
    }

    /**
     * 获取目标下的所有任务
     */
    public TaskListResponse listByGoal(UUID goalId) {
        List<PlanningTask> tasks = taskRepository.findByGoalId(goalId);

        List<TaskRecord> records = tasks.stream()
                .map(this::toTaskRecord)
                .collect(Collectors.toList());

        return TaskListResponse.of(records);
    }

    /**
     * 创建单个任务
     */
    @Transactional
    public TaskRecord create(TaskRecord.Create request) {
        PlanningTask task = new PlanningTask();
        task.goalId = request.goalId();
        task.description = request.description();
        task.priority = request.priority() != null ? request.priority() : TaskPriority.NORMAL;
        task.parameters = request.parameters();
        task.executorType = request.executorType();
        task.maxRetryCount = request.maxRetryCount() != null ? request.maxRetryCount() : 3;
        task.status = PlanningTaskStatus.CREATED;
        task.depth = 0;

        taskRepository.persist(task);

        Log.infof("Task created: %s", task.id);
        return toTaskRecord(task);
    }

    /**
     * 调度目标下的任务
     */
    @Transactional
    public List<TaskRecord> scheduleGoal(UUID goalId) {
        List<PlanningTask> tasks = scheduler.scheduleGoal(goalId);

        return tasks.stream()
                .map(this::toTaskRecord)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务状态统计
     */
    public TaskRecord.TaskCounts getCounts() {
        return new TaskRecord.TaskCounts(
                taskRepository.countByStatus(PlanningTaskStatus.CREATED),
                taskRepository.countByStatus(PlanningTaskStatus.READY),
                taskRepository.countByStatus(PlanningTaskStatus.SCHEDULED),
                taskRepository.countByStatus(PlanningTaskStatus.RUNNING),
                taskRepository.countByStatus(PlanningTaskStatus.COMPLETED),
                taskRepository.countByStatus(PlanningTaskStatus.FAILED),
                taskRepository.countByStatus(PlanningTaskStatus.CANCELLED)
        );
    }

    /**
     * 转换为 DTO
     */
    private TaskRecord toTaskRecord(PlanningTask task) {
        List<UUID> dependencies = dependencyRepository.findByTaskId(task.id)
                .stream()
                .map(d -> d.dependsOnTask.id)
                .collect(Collectors.toList());

        return new TaskRecord(
                task.id,
                task.goalId,
                task.description,
                task.status,
                task.priority,
                task.priorityValue,
                task.parentTask != null ? task.parentTask.id : null,
                task.depth,
                task.estimatedDurationSeconds,
                task.actualDurationSeconds,
                task.parameters,
                task.result,
                task.errorMessage,
                task.retryCount,
                task.maxRetryCount,
                task.executorType,
                task.createdAt,
                task.updatedAt,
                task.startedAt,
                task.completedAt,
                dependencies
        );
    }
}