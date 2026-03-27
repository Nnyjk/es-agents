package com.easystation.agent.record;

import com.easystation.agent.domain.ScheduledTask;
import com.easystation.agent.domain.ScheduledTaskExecution;
import com.easystation.agent.domain.enums.ScheduledTaskCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 定时任务 Record 类
 */
public class ScheduledTaskRecord {

    /**
     * 列表响应
     */
    public record ListResponse(
            UUID id,
            String name,
            String description,
            ScheduledTaskCategory category,
            String cronExpression,
            String tags,
            Long timeout,
            Integer retryCount,
            Long retryInterval,
            Boolean isActive,
            LocalDateTime lastExecutedAt,
            LocalDateTime nextExecutionAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ListResponse from(ScheduledTask task) {
            return new ListResponse(
                    task.id,
                    task.name,
                    task.description,
                    task.category,
                    task.cronExpression,
                    task.tags,
                    task.timeout,
                    task.retryCount,
                    task.retryInterval,
                    task.isActive,
                    task.lastExecutedAt,
                    task.nextExecutionAt,
                    task.createdAt,
                    task.updatedAt
            );
        }

        public static List<ListResponse> from(List<ScheduledTask> tasks) {
            return tasks.stream().map(ListResponse::from).toList();
        }
    }

    /**
     * 详情响应
     */
    public record DetailResponse(
            UUID id,
            String name,
            String description,
            ScheduledTaskCategory category,
            String cronExpression,
            String tags,
            Long timeout,
            Integer retryCount,
            Long retryInterval,
            Boolean isActive,
            String parameters,
            String createdBy,
            LocalDateTime lastExecutedAt,
            LocalDateTime nextExecutionAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static DetailResponse from(ScheduledTask task) {
            return new DetailResponse(
                    task.id,
                    task.name,
                    task.description,
                    task.category,
                    task.cronExpression,
                    task.tags,
                    task.timeout,
                    task.retryCount,
                    task.retryInterval,
                    task.isActive,
                    task.parameters,
                    task.createdBy,
                    task.lastExecutedAt,
                    task.nextExecutionAt,
                    task.createdAt,
                    task.updatedAt
            );
        }
    }

    /**
     * 创建请求
     */
    public record CreateRequest(
            String name,
            String description,
            String script,
            String cronExpression,
            ScheduledTaskCategory category,
            String tags,
            Long timeout,
            Integer retryCount,
            Long retryInterval,
            String parameters,
            String createdBy
    ) {
        public CreateRequest {
            if (timeout == null) timeout = 300L;
            if (retryCount == null) retryCount = 0;
            if (retryInterval == null) retryInterval = 60L;
            if (category == null) category = ScheduledTaskCategory.CUSTOM;
        }

        public ScheduledTask toEntity() {
            ScheduledTask task = new ScheduledTask();
            task.name = this.name;
            task.description = this.description;
            task.script = this.script;
            task.cronExpression = this.cronExpression;
            task.category = this.category;
            task.tags = this.tags;
            task.timeout = this.timeout;
            task.retryCount = this.retryCount;
            task.retryInterval = this.retryInterval;
            task.parameters = this.parameters;
            task.createdBy = this.createdBy;
            task.isActive = true;
            return task;
        }
    }

    /**
     * 更新请求
     */
    public record UpdateRequest(
            String name,
            String description,
            String script,
            String cronExpression,
            ScheduledTaskCategory category,
            String tags,
            Long timeout,
            Integer retryCount,
            Long retryInterval,
            String parameters
    ) {
    }

    /**
     * Cron 验证请求
     */
    public record CronValidateRequest(
            String cronExpression
    ) {
    }

    /**
     * Cron 验证响应
     */
    public record CronValidateResponse(
            boolean valid,
            String message,
            LocalDateTime nextFireTime
    ) {
    }
}
