package com.easystation.scheduler.service;

import com.easystation.scheduler.domain.ScheduledTask;
import com.easystation.scheduler.domain.TaskExecution;
import com.easystation.scheduler.dto.ScheduledTaskRecord;
import com.easystation.scheduler.enums.ExecutionStatus;
import com.easystation.scheduler.enums.TaskStatus;
import com.easystation.scheduler.enums.TaskType;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ScheduledTaskService {

    public List<ScheduledTaskRecord.Detail> list(ScheduledTaskRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and name like :keyword");
            params.put("keyword", "%" + query.keyword() + "%");
        }
        if (query.type() != null) {
            sql.append(" and type = :type");
            params.put("type", query.type());
        }
        if (query.status() != null) {
            sql.append(" and status = :status");
            params.put("status", query.status());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return ScheduledTask.<ScheduledTask>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public ScheduledTaskRecord.Detail get(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        return toDetail(task);
    }

    @Transactional
    public ScheduledTaskRecord.Detail create(ScheduledTaskRecord.Create dto) {
        if (!isValidCron(dto.cronExpression())) {
            throw new WebApplicationException("Invalid cron expression", Response.Status.BAD_REQUEST);
        }

        ScheduledTask task = new ScheduledTask();
        task.name = dto.name();
        task.type = dto.type();
        task.description = dto.description();
        task.cronExpression = dto.cronExpression();
        task.status = TaskStatus.ENABLED;
        task.config = dto.config();
        task.targetId = dto.targetId();
        task.targetType = dto.targetType();
        task.maxRetries = dto.maxRetries() != null ? dto.maxRetries() : 3;
        task.timeoutSeconds = dto.timeoutSeconds() != null ? dto.timeoutSeconds() : 300;
        task.alertOnFailure = dto.alertOnFailure() != null ? dto.alertOnFailure() : true;
        task.createdBy = dto.createdBy();
        task.nextExecutionAt = calculateNextExecution(dto.cronExpression());
        task.persist();

        Log.infof("Scheduled task created: %s", task.name);
        return toDetail(task);
    }

    @Transactional
    public ScheduledTaskRecord.Detail update(UUID id, ScheduledTaskRecord.Update dto) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }

        if (dto.cronExpression() != null && !isValidCron(dto.cronExpression())) {
            throw new WebApplicationException("Invalid cron expression", Response.Status.BAD_REQUEST);
        }

        if (dto.name() != null) task.name = dto.name();
        if (dto.description() != null) task.description = dto.description();
        if (dto.cronExpression() != null) {
            task.cronExpression = dto.cronExpression();
            task.nextExecutionAt = calculateNextExecution(dto.cronExpression());
        }
        if (dto.status() != null) task.status = dto.status();
        if (dto.config() != null) task.config = dto.config();
        if (dto.targetId() != null) task.targetId = dto.targetId();
        if (dto.targetType() != null) task.targetType = dto.targetType();
        if (dto.maxRetries() != null) task.maxRetries = dto.maxRetries();
        if (dto.timeoutSeconds() != null) task.timeoutSeconds = dto.timeoutSeconds();
        if (dto.alertOnFailure() != null) task.alertOnFailure = dto.alertOnFailure();

        return toDetail(task);
    }

    @Transactional
    public void delete(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        TaskExecution.delete("taskId", id);
        task.delete();
        Log.infof("Scheduled task deleted: %s", task.name);
    }

    @Transactional
    public ScheduledTaskRecord.Detail enable(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        task.status = TaskStatus.ENABLED;
        task.nextExecutionAt = calculateNextExecution(task.cronExpression);
        return toDetail(task);
    }

    @Transactional
    public ScheduledTaskRecord.Detail disable(UUID id) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        task.status = TaskStatus.DISABLED;
        task.nextExecutionAt = null;
        return toDetail(task);
    }

    @Transactional
    public ScheduledTaskRecord.ExecutionDetail executeNow(UUID id, ScheduledTaskRecord.ExecuteRequest dto) {
        ScheduledTask task = ScheduledTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }

        TaskExecution execution = new TaskExecution();
        execution.taskId = id;
        execution.status = ExecutionStatus.PENDING;
        execution.scheduledAt = LocalDateTime.now();
        execution.triggerType = "MANUAL";
        execution.triggeredBy = dto.triggeredBy();
        execution.retryCount = 0;
        execution.persist();

        // Execute task asynchronously (simplified)
        executeTask(task, execution);

        return toExecutionDetail(execution, task.name);
    }

    public List<ScheduledTaskRecord.ExecutionDetail> getExecutions(ScheduledTaskRecord.ExecutionQuery query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.taskId() != null) {
            sql.append(" and taskId = :taskId");
            params.put("taskId", query.taskId());
        }
        if (query.status() != null) {
            sql.append(" and status = :status");
            params.put("status", query.status());
        }
        if (query.triggerType() != null && !query.triggerType().isBlank()) {
            sql.append(" and triggerType = :triggerType");
            params.put("triggerType", query.triggerType());
        }
        if (query.startTime() != null) {
            sql.append(" and createdAt >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and createdAt <= :endTime");
            params.put("endTime", query.endTime());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return TaskExecution.<TaskExecution>find(sql.toString(), Sort.by("createdAt").descending(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(e -> {
                    ScheduledTask task = ScheduledTask.findById(e.taskId);
                    return toExecutionDetail(e, task != null ? task.name : "Unknown");
                })
                .collect(Collectors.toList());
    }

    public ScheduledTaskRecord.ExecutionDetail getExecution(UUID executionId) {
        TaskExecution execution = TaskExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException("Execution not found", Response.Status.NOT_FOUND);
        }
        ScheduledTask task = ScheduledTask.findById(execution.taskId);
        return toExecutionDetail(execution, task != null ? task.name : "Unknown");
    }

    public ScheduledTaskRecord.CronValidation validateCron(String cronExpression) {
        try {
            LocalDateTime nextTime = calculateNextExecution(cronExpression);
            return new ScheduledTaskRecord.CronValidation(true, "Valid cron expression", nextTime);
        } catch (Exception e) {
            return new ScheduledTaskRecord.CronValidation(false, "Invalid cron expression: " + e.getMessage(), null);
        }
    }

    public ScheduledTaskRecord.TaskStats getStats() {
        long totalTasks = ScheduledTask.count();
        long enabledTasks = ScheduledTask.count("status", TaskStatus.ENABLED);
        long disabledTasks = ScheduledTask.count("status", TaskStatus.DISABLED);
        long totalExecutions = TaskExecution.count();
        long successExecutions = TaskExecution.count("status", ExecutionStatus.SUCCESS);
        long failedExecutions = TaskExecution.count("status", ExecutionStatus.FAILED);

        return new ScheduledTaskRecord.TaskStats(
                totalTasks, enabledTasks, disabledTasks,
                totalExecutions, successExecutions, failedExecutions
        );
    }

    @Scheduled(every = "1m")
    @Transactional
    public void processScheduledTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTask> tasks = ScheduledTask.<ScheduledTask>find(
                "status = ?1 and nextExecutionAt <= ?2", 
                TaskStatus.ENABLED, now
        ).list();

        for (ScheduledTask task : tasks) {
            Log.debugf("Processing scheduled task: %s", task.name);
            
            TaskExecution execution = new TaskExecution();
            execution.taskId = task.id;
            execution.status = ExecutionStatus.PENDING;
            execution.scheduledAt = now;
            execution.triggerType = "SCHEDULED";
            execution.triggeredBy = "system";
            execution.retryCount = 0;
            execution.persist();

            executeTask(task, execution);

            task.lastExecutionAt = now;
            task.lastExecutionStatus = execution.status.name();
            task.nextExecutionAt = calculateNextExecution(task.cronExpression);
        }
    }

    private void executeTask(ScheduledTask task, TaskExecution execution) {
        try {
            execution.status = ExecutionStatus.RUNNING;
            execution.startedAt = LocalDateTime.now();

            // Simulate task execution based on type
            String result = switch (task.type) {
                case COMMAND -> executeCommand(task);
                case DEPLOYMENT -> executeDeployment(task);
                case BACKUP -> executeBackup(task);
                case SYNC -> executeSync(task);
                case CLEANUP -> executeCleanup(task);
                case CUSTOM -> executeCustom(task);
            };

            execution.status = ExecutionStatus.SUCCESS;
            execution.result = result;
            execution.logs = "Task executed successfully";
        } catch (Exception e) {
            execution.status = ExecutionStatus.FAILED;
            execution.errorMessage = e.getMessage();
            execution.logs = "Task execution failed: " + e.getMessage();
            
            if (task.alertOnFailure) {
                Log.warnf("Scheduled task failed: %s, error: %s", task.name, e.getMessage());
            }
        } finally {
            execution.finishedAt = LocalDateTime.now();
            execution.durationMs = java.time.Duration.between(execution.startedAt, execution.finishedAt).toMillis();
        }
    }

    private String executeCommand(ScheduledTask task) {
        Log.infof("Executing command task: %s", task.name);
        return "Command executed";
    }

    private String executeDeployment(ScheduledTask task) {
        Log.infof("Executing deployment task: %s", task.name);
        return "Deployment completed";
    }

    private String executeBackup(ScheduledTask task) {
        Log.infof("Executing backup task: %s", task.name);
        return "Backup completed";
    }

    private String executeSync(ScheduledTask task) {
        Log.infof("Executing sync task: %s", task.name);
        return "Sync completed";
    }

    private String executeCleanup(ScheduledTask task) {
        Log.infof("Executing cleanup task: %s", task.name);
        return "Cleanup completed";
    }

    private String executeCustom(ScheduledTask task) {
        Log.infof("Executing custom task: %s", task.name);
        return "Custom task completed";
    }

    private boolean isValidCron(String cronExpression) {
        try {
            org.quartz.CronExpression.validateExpression(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDateTime calculateNextExecution(String cronExpression) {
        try {
            org.quartz.CronExpression cron = new org.quartz.CronExpression(cronExpression);
            Date nextTime = cron.getNextValidTimeAfter(new Date());
            return nextTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private ScheduledTaskRecord.Detail toDetail(ScheduledTask task) {
        return new ScheduledTaskRecord.Detail(
                task.id,
                task.name,
                task.type,
                task.description,
                task.cronExpression,
                task.status,
                task.config,
                task.targetId,
                task.targetType,
                task.maxRetries,
                task.timeoutSeconds,
                task.alertOnFailure,
                task.lastExecutionAt,
                task.lastExecutionStatus,
                task.nextExecutionAt,
                task.createdBy,
                task.createdAt,
                task.updatedAt
        );
    }

    private ScheduledTaskRecord.ExecutionDetail toExecutionDetail(TaskExecution execution, String taskName) {
        return new ScheduledTaskRecord.ExecutionDetail(
                execution.id,
                execution.taskId,
                taskName,
                execution.status,
                execution.startedAt,
                execution.finishedAt,
                execution.durationMs,
                execution.scheduledAt,
                execution.triggerType,
                execution.triggeredBy,
                execution.result,
                execution.logs,
                execution.errorMessage,
                execution.retryCount,
                execution.createdAt
        );
    }
}