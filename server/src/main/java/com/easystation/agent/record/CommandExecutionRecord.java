package com.easystation.agent.record;

import com.easystation.agent.domain.CommandExecution;
import com.easystation.agent.domain.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class CommandExecutionRecord {

    public record ExecuteRequest(
            UUID agentInstanceId,
            UUID templateId,
            String command,
            Map<String, Object> parameters,
            Long timeout
    ) {}

    public record ExecuteResponse(
            UUID executionId,
            String message
    ) {}

    public record ListQuery(
            UUID agentInstanceId,
            UUID templateId,
            ExecutionStatus status,
            String executedBy,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer page,
            Integer size
    ) {}

    public record ListResponse(
            UUID id,
            UUID templateId,
            String templateName,
            UUID agentInstanceId,
            String hostName,
            String command,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Integer exitCode,
            LocalDateTime createdAt,
            String executedBy
    ) {
        public static ListResponse from(CommandExecution execution) {
            return new ListResponse(
                    execution.id,
                    execution.template != null ? execution.template.id : null,
                    execution.template != null ? execution.template.name : null,
                    execution.agentInstance != null ? execution.agentInstance.id : null,
                    execution.agentInstance != null && execution.agentInstance.host != null 
                            ? execution.agentInstance.host.name : null,
                    execution.command,
                    execution.status,
                    execution.startedAt,
                    execution.finishedAt,
                    execution.exitCode,
                    execution.createdAt,
                    execution.executedBy
            );
        }
    }

    public record DetailResponse(
            UUID id,
            UUID templateId,
            String templateName,
            UUID agentInstanceId,
            String hostName,
            String command,
            String parameters,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            String output,
            Integer exitCode,
            String errorMessage,
            Integer retryCount,
            LocalDateTime createdAt,
            String executedBy
    ) {
        public static DetailResponse from(CommandExecution execution) {
            return new DetailResponse(
                    execution.id,
                    execution.template != null ? execution.template.id : null,
                    execution.template != null ? execution.template.name : null,
                    execution.agentInstance != null ? execution.agentInstance.id : null,
                    execution.agentInstance != null && execution.agentInstance.host != null 
                            ? execution.agentInstance.host.name : null,
                    execution.command,
                    execution.parameters,
                    execution.status,
                    execution.startedAt,
                    execution.finishedAt,
                    execution.output,
                    execution.exitCode,
                    execution.errorMessage,
                    execution.retryCount,
                    execution.createdAt,
                    execution.executedBy
            );
        }
    }

    /**
     * Agent 回调请求 - 任务执行结果上报
     */
    public record CallbackRequest(
            String status,        // SUCCESS, FAILED, TIMEOUT, CANCELLED
            Integer exitCode,     // 退出码
            String output,        // 执行输出
            Long durationMs       // 执行耗时（毫秒）
    ) {}

    /**
     * Agent 回调响应
     */
    public record CallbackResponse(
            boolean success,
            String message
    ) {}
}
