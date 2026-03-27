package com.easystation.agent.record;

import com.easystation.agent.domain.ScheduledTask;
import com.easystation.agent.domain.ScheduledTaskExecution;
import com.easystation.agent.domain.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 定时任务执行记录 Record 类
 */
public class ScheduledTaskExecutionRecord {

    /**
     * 列表响应
     */
    public record ListResponse(
            UUID id,
            UUID taskId,
            String taskName,
            UUID agentInstanceId,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Integer exitCode,
            String errorMessage,
            Integer retryCount,
            String triggeredBy,
            LocalDateTime createdAt
    ) {
        public static ListResponse from(ScheduledTaskExecution execution) {
            return new ListResponse(
                    execution.id,
                    execution.scheduledTask != null ? execution.scheduledTask.id : null,
                    execution.scheduledTask != null ? execution.scheduledTask.name : null,
                    execution.agentInstanceId,
                    execution.status,
                    execution.startedAt,
                    execution.finishedAt,
                    execution.exitCode,
                    execution.errorMessage,
                    execution.retryCount,
                    execution.triggeredBy,
                    execution.createdAt
            );
        }

        public static List<ListResponse> from(List<ScheduledTaskExecution> executions) {
            return executions.stream().map(ListResponse::from).toList();
        }
    }

    /**
     * 详情响应
     */
    public record DetailResponse(
            UUID id,
            UUID taskId,
            String taskName,
            UUID agentInstanceId,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String output,
            Integer exitCode,
            String errorMessage,
            Integer retryCount,
            String triggeredBy,
            LocalDateTime createdAt
    ) {
        public static DetailResponse from(ScheduledTaskExecution execution) {
            return new DetailResponse(
                    execution.id,
                    execution.scheduledTask != null ? execution.scheduledTask.id : null,
                    execution.scheduledTask != null ? execution.scheduledTask.name : null,
                    execution.agentInstanceId,
                    execution.status,
                    execution.startedAt,
                    execution.finishedAt,
                    execution.output,
                    execution.exitCode,
                    execution.errorMessage,
                    execution.retryCount,
                    execution.triggeredBy,
                    execution.createdAt
            );
        }
    }

    /**
     * 创建执行记录请求
     */
    public record CreateRequest(
            UUID taskId,
            UUID agentInstanceId,
            String triggeredBy
    ) {
        public CreateRequest {
            if (triggeredBy == null) triggeredBy = "SCHEDULED";
        }

        public ScheduledTaskExecution toEntity(ScheduledTask task) {
            ScheduledTaskExecution execution = new ScheduledTaskExecution();
            execution.scheduledTask = task;
            execution.agentInstanceId = this.agentInstanceId;
            execution.triggeredBy = this.triggeredBy;
            execution.status = ExecutionStatus.PENDING;
            execution.retryCount = 0;
            return execution;
        }
    }

    /**
     * 更新执行状态请求
     */
    public record UpdateStatusRequest(
            ExecutionStatus status,
            String output,
            Integer exitCode,
            String errorMessage
    ) {
    }
}
