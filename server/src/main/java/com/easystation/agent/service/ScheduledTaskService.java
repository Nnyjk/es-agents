package com.easystation.agent.service;

import com.easystation.agent.domain.ScheduledTask;
import com.easystation.agent.domain.ScheduledTaskExecution;
import com.easystation.agent.domain.enums.ExecutionStatus;
import com.easystation.agent.domain.enums.ScheduledTaskCategory;
import com.easystation.agent.record.ScheduledTaskExecutionRecord;
import com.easystation.agent.record.ScheduledTaskRecord;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 定时任务服务类
 * 提供定时任务的管理、调度、执行等功能
 */
@ApplicationScoped
public class ScheduledTaskService {

    private static final Pattern CRON_PATTERN = Pattern.compile(
            "^([0-9*,/-]+|\\*)\\s+([0-9*,/-]+|\\*)\\s+([0-9*,/-]+|\\*)\\s+([0-9*,/-]+|\\*)\\s+([0-9*,/-]+|\\*)(\\s+([0-9*,/-]+|\\*))?$"
    );

    /**
     * 查询定时任务列表
     *
     * @param category 任务分类（可选）
     * @param activeOnly 是否只查询启用的任务
     * @return 任务列表
     */
    public List<ScheduledTaskRecord.ListResponse> list(ScheduledTaskCategory category, Boolean activeOnly) {
        List<ScheduledTask> tasks;

        if (category != null && activeOnly != null) {
            tasks = ScheduledTask.find("category = ?1 and isActive = ?2", category, activeOnly).list();
        } else if (category != null) {
            tasks = ScheduledTask.find("category", category).list();
        } else if (activeOnly != null) {
            tasks = ScheduledTask.find("isActive = ?1", activeOnly).list();
        } else {
            tasks = ScheduledTask.listAll();
        }

        return ScheduledTaskRecord.ListResponse.from(tasks);
    }

    /**
     * 查询定时任务详情
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    public ScheduledTaskRecord.DetailResponse getById(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Scheduled task not found: " + id)
                            .build()
            );
        }
        return ScheduledTaskRecord.DetailResponse.from(task);
    }

    /**
     * 创建定时任务
     *
     * @param request 创建请求
     * @return 创建后的任务
     */
    @Transactional
    public ScheduledTaskRecord.DetailResponse create(ScheduledTaskRecord.CreateRequest request) {
        // 验证 Cron 表达式
        if (!isValidCronExpression(request.cronExpression())) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid cron expression: " + request.cronExpression())
                            .build()
            );
        }

        // 检查名称是否已存在
        if (ScheduledTask.findByName(request.name()) != null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .entity("Task name already exists: " + request.name())
                            .build()
            );
        }

        ScheduledTask task = request.toEntity();
        task.persist();

        Log.infof("Created scheduled task: %s (id=%s)", task.name, task.id);
        return ScheduledTaskRecord.DetailResponse.from(task);
    }

    /**
     * 更新定时任务
     *
     * @param id 任务 ID
     * @param request 更新请求
     * @return 更新后的任务
     */
    @Transactional
    public ScheduledTaskRecord.DetailResponse update(UUID id, ScheduledTaskRecord.UpdateRequest request) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Scheduled task not found: " + id)
                            .build()
            );
        }

        // 如果更新了 Cron 表达式，验证其有效性
        if (request.cronExpression() != null && !request.cronExpression().equals(task.cronExpression)) {
            if (!isValidCronExpression(request.cronExpression())) {
                throw new WebApplicationException(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid cron expression: " + request.cronExpression())
                                .build()
                );
            }
            task.cronExpression = request.cronExpression();
        }

        if (request.name() != null) task.name = request.name();
        if (request.description() != null) task.description = request.description();
        if (request.script() != null) task.script = request.script();
        if (request.category() != null) task.category = request.category();
        if (request.tags() != null) task.tags = request.tags();
        if (request.timeout() != null) task.timeout = request.timeout();
        if (request.retryCount() != null) task.retryCount = request.retryCount();
        if (request.retryInterval() != null) task.retryInterval = request.retryInterval();
        if (request.parameters() != null) task.parameters = request.parameters();

        task.persist();

        Log.infof("Updated scheduled task: %s (id=%s)", task.name, task.id);
        return ScheduledTaskRecord.DetailResponse.from(task);
    }

    /**
     * 删除定时任务
     *
     * @param id 任务 ID
     */
    @Transactional
    public void delete(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Scheduled task not found: " + id)
                            .build()
            );
        }

        Log.infof("Deleting scheduled task: %s (id=%s)", task.name, task.id);
        task.delete();
    }

    /**
     * 启用定时任务
     *
     * @param id 任务 ID
     * @return 启用后的任务
     */
    @Transactional
    public ScheduledTaskRecord.DetailResponse enable(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Scheduled task not found: " + id)
                            .build()
            );
        }

        task.isActive = true;
        task.persist();

        Log.infof("Enabled scheduled task: %s (id=%s)", task.name, task.id);
        return ScheduledTaskRecord.DetailResponse.from(task);
    }

    /**
     * 禁用定时任务
     *
     * @param id 任务 ID
     * @return 禁用后的任务
     */
    @Transactional
    public ScheduledTaskRecord.DetailResponse disable(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Scheduled task not found: " + id)
                            .build()
            );
        }

        task.isActive = false;
        task.persist();

        Log.infof("Disabled scheduled task: %s (id=%s)", task.name, task.id);
        return ScheduledTaskRecord.DetailResponse.from(task);
    }

    /**
     * 立即执行定时任务
     *
     * @param id 任务 ID
     * @param agentInstanceId 执行节点 ID
     * @return 执行记录
     */
    @Transactional
    public ScheduledTaskExecutionRecord.DetailResponse executeNow(UUID id, UUID agentInstanceId) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Scheduled task not found: " + id)
                            .build()
            );
        }

        // 创建执行记录
        ScheduledTaskExecution execution = new ScheduledTaskExecution();
        execution.scheduledTask = task;
        execution.agentInstanceId = agentInstanceId;
        execution.triggeredBy = "MANUAL";
        execution.status = ExecutionStatus.PENDING;
        execution.retryCount = 0;
        execution.persist();

        Log.infof("Scheduled task executed manually: %s (executionId=%s)", task.name, execution.id);
        return ScheduledTaskExecutionRecord.DetailResponse.from(execution);
    }

    /**
     * 查询任务的执行历史
     *
     * @param taskId 任务 ID
     * @return 执行记录列表
     */
    public List<ScheduledTaskExecutionRecord.ListResponse> getExecutionHistory(UUID taskId) {
        List<ScheduledTaskExecution> executions = ScheduledTaskExecution.findByTaskId(taskId);
        return ScheduledTaskExecutionRecord.ListResponse.from(executions);
    }

    /**
     * 查询执行记录详情
     *
     * @param executionId 执行记录 ID
     * @return 执行记录详情
     */
    public ScheduledTaskExecutionRecord.DetailResponse getExecutionById(UUID executionId) {
        ScheduledTaskExecution execution = ScheduledTaskExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Execution record not found: " + executionId)
                            .build()
            );
        }
        return ScheduledTaskExecutionRecord.DetailResponse.from(execution);
    }

    /**
     * 更新执行状态
     *
     * @param executionId 执行记录 ID
     * @param request 更新请求
     * @return 更新后的执行记录
     */
    @Transactional
    public ScheduledTaskExecutionRecord.DetailResponse updateExecutionStatus(
            UUID executionId,
            ScheduledTaskExecutionRecord.UpdateStatusRequest request
    ) {
        ScheduledTaskExecution execution = ScheduledTaskExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("Execution record not found: " + executionId)
                            .build()
            );
        }

        execution.status = request.status();
        if (request.output() != null) execution.output = request.output();
        if (request.exitCode() != null) execution.exitCode = request.exitCode();
        if (request.errorMessage() != null) execution.errorMessage = request.errorMessage();

        if (request.status() == ExecutionStatus.RUNNING) {
            execution.startedAt = LocalDateTime.now();
        } else if (request.status() == ExecutionStatus.SUCCESS ||
                request.status() == ExecutionStatus.FAILED ||
                request.status() == ExecutionStatus.TIMEOUT) {
            execution.finishedAt = LocalDateTime.now();

            // 如果是成功执行，更新任务的最后执行时间
            if (request.status() == ExecutionStatus.SUCCESS) {
                execution.scheduledTask.lastExecutedAt = LocalDateTime.now();
            }
        }

        execution.persist();

        Log.infof("Updated execution status: %s (id=%s)", request.status(), executionId);
        return ScheduledTaskExecutionRecord.DetailResponse.from(execution);
    }

    /**
     * 验证 Cron 表达式
     *
     * @param cronExpression Cron 表达式
     * @return 是否有效
     */
    public boolean isValidCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return false;
        }
        return CRON_PATTERN.matcher(cronExpression.trim()).matches();
    }

    /**
     * 查找需要执行的任务
     *
     * @return 待执行任务列表
     */
    public List<ScheduledTaskRecord.ListResponse> findDueTasks() {
        List<ScheduledTask> tasks = ScheduledTask.findDueTasks();
        return ScheduledTaskRecord.ListResponse.from(tasks);
    }
}
