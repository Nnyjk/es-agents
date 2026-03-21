package com.easystation.agent.record;

import com.easystation.agent.domain.CommandExecution;
import com.easystation.agent.domain.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommandExecutionRecord {

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
}