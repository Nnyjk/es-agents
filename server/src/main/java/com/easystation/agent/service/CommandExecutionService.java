package com.easystation.agent.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.CommandExecution;
import com.easystation.agent.domain.CommandTemplate;
import com.easystation.agent.domain.enums.ExecutionStatus;
import com.easystation.agent.dto.AgentCommandMessage;
import com.easystation.agent.record.CommandExecutionRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.infra.domain.Host;
import com.easystation.infra.socket.AgentConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandExecutionService {

    @Inject
    AgentConnectionManager connectionManager;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AuditLogService auditLogService;

    private static final long DEFAULT_TIMEOUT = 300L;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Execute a command on an agent instance.
     * Supports both template-based and direct command execution.
     */
    @Transactional
    public CommandExecutionRecord.ExecuteResponse execute(
            CommandExecutionRecord.ExecuteRequest request,
            String username,
            String clientIp) {

        AgentInstance instance = AgentInstance.findById(request.agentInstanceId());
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        if (instance.host == null) {
            throw new WebApplicationException("Agent instance has no associated host", Response.Status.BAD_REQUEST);
        }

        Host host = instance.host;

        // Determine command and timeout
        String command;
        Long timeout;
        CommandTemplate template = null;

        if (request.templateId() != null) {
            template = CommandTemplate.findById(request.templateId());
            if (template == null) {
                throw new WebApplicationException("Command template not found", Response.Status.NOT_FOUND);
            }
            if (!template.isActive) {
                throw new WebApplicationException("Command template is not active", Response.Status.BAD_REQUEST);
            }
            command = substituteParameters(template.script, request.parameters());
            timeout = template.timeout;
        } else {
            if (request.command() == null || request.command().isBlank()) {
                throw new WebApplicationException("Command is required when templateId is not provided", Response.Status.BAD_REQUEST);
            }
            command = request.command();
            timeout = request.timeout() != null ? request.timeout() : DEFAULT_TIMEOUT;
        }

        // Create execution record
        CommandExecution execution = new CommandExecution();
        execution.template = template;
        execution.agentInstance = instance;
        execution.command = command;
        execution.parameters = request.parameters() != null ? request.parameters().toString() : null;
        execution.status = ExecutionStatus.PENDING;
        execution.executedBy = username;
        execution.startedAt = LocalDateTime.now();
        execution.retryCount = 0;
        execution.persist();

        // Send command to agent
        boolean sent = sendCommandToAgent(host.id, execution.id, command, timeout);

        if (sent) {
            execution.status = ExecutionStatus.RUNNING;
            Log.infof("Command execution %s sent to agent %s", execution.id, request.agentInstanceId());
            recordAuditLog(username, null, AuditAction.EXECUTE_COMMAND, AuditResult.SUCCESS,
                    "Command execution started", "CommandExecution", execution.id, clientIp, "/api/v1/agent-commands/execute");
        } else {
            execution.status = ExecutionStatus.FAILED;
            execution.errorMessage = "Failed to send command to agent: no active connection";
            execution.finishedAt = LocalDateTime.now();
            Log.errorf("Failed to send command execution %s to agent %s", execution.id, request.agentInstanceId());
            recordAuditLog(username, null, AuditAction.EXECUTE_COMMAND, AuditResult.FAILED,
                    "Failed to send command to agent", "CommandExecution", execution.id, clientIp, "/api/v1/agent-commands/execute");
        }

        execution.persist();
        return new CommandExecutionRecord.ExecuteResponse(execution.id, execution.status.name());
    }

    /**
     * Get execution status by ID.
     */
    public CommandExecutionRecord.DetailResponse getStatus(UUID executionId) {
        CommandExecution execution = CommandExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException("Command execution not found", Response.Status.NOT_FOUND);
        }
        return CommandExecutionRecord.DetailResponse.from(execution);
    }

    /**
     * Retry a failed execution.
     */
    @Transactional
    public CommandExecutionRecord.ExecuteResponse retry(UUID executionId, String username, String clientIp) {
        CommandExecution original = CommandExecution.findById(executionId);
        if (original == null) {
            throw new WebApplicationException("Command execution not found", Response.Status.NOT_FOUND);
        }

        if (original.status != ExecutionStatus.FAILED && original.status != ExecutionStatus.TIMEOUT) {
            throw new WebApplicationException("Only failed or timed out executions can be retried", Response.Status.BAD_REQUEST);
        }

        AgentInstance instance = original.agentInstance;
        if (instance == null || instance.host == null) {
            throw new WebApplicationException("Original execution has no valid agent instance", Response.Status.BAD_REQUEST);
        }

        // Create new execution record
        CommandExecution retryExecution = new CommandExecution();
        retryExecution.template = original.template;
        retryExecution.agentInstance = instance;
        retryExecution.command = original.command;
        retryExecution.parameters = original.parameters;
        retryExecution.status = ExecutionStatus.PENDING;
        retryExecution.executedBy = username;
        retryExecution.startedAt = LocalDateTime.now();
        retryExecution.retryCount = (original.retryCount != null ? original.retryCount : 0) + 1;
        retryExecution.persist();

        // Determine timeout
        Long timeout = DEFAULT_TIMEOUT;
        if (original.template != null && original.template.timeout != null) {
            timeout = original.template.timeout;
        }

        // Send command to agent
        boolean sent = sendCommandToAgent(instance.host.id, retryExecution.id, original.command, timeout);

        if (sent) {
            retryExecution.status = ExecutionStatus.RUNNING;
            Log.infof("Retry execution %s sent to agent %s (retry count: %d)",
                    retryExecution.id, instance.id, retryExecution.retryCount);
            recordAuditLog(username, null, AuditAction.EXECUTE_COMMAND, AuditResult.SUCCESS,
                    "Command retry started", "CommandExecution", retryExecution.id, clientIp,
                    "/api/v1/agent-commands/" + executionId + "/retry");
        } else {
            retryExecution.status = ExecutionStatus.FAILED;
            retryExecution.errorMessage = "Failed to send command to agent: no active connection";
            retryExecution.finishedAt = LocalDateTime.now();
            recordAuditLog(username, null, AuditAction.EXECUTE_COMMAND, AuditResult.FAILED,
                    "Failed to retry command", "CommandExecution", retryExecution.id, clientIp,
                    "/api/v1/agent-commands/" + executionId + "/retry");
        }

        retryExecution.persist();
        return new CommandExecutionRecord.ExecuteResponse(retryExecution.id, retryExecution.status.name());
    }

    /**
     * List execution history with pagination and filtering.
     */
    public List<CommandExecutionRecord.ListResponse> list(
            UUID agentInstanceId,
            UUID templateId,
            ExecutionStatus status,
            String executedBy,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer page,
            Integer size) {

        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (agentInstanceId != null) {
            queryBuilder.append(" and agentInstance.id = :agentInstanceId");
            params.put("agentInstanceId", agentInstanceId);
        }
        if (templateId != null) {
            queryBuilder.append(" and template.id = :templateId");
            params.put("templateId", templateId);
        }
        if (status != null) {
            queryBuilder.append(" and status = :status");
            params.put("status", status);
        }
        if (executedBy != null && !executedBy.isBlank()) {
            queryBuilder.append(" and executedBy = :executedBy");
            params.put("executedBy", executedBy);
        }
        if (startTime != null) {
            queryBuilder.append(" and createdAt >= :startTime");
            params.put("startTime", startTime);
        }
        if (endTime != null) {
            queryBuilder.append(" and createdAt <= :endTime");
            params.put("endTime", endTime);
        }

        int pageSize = size != null ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        int pageIndex = page != null ? page : 0;

        List<CommandExecution> executions = CommandExecution.find(queryBuilder.toString(), Sort.descending("createdAt"), params)
                .page(Page.of(pageIndex, pageSize))
                .list();

        return executions.stream()
                .map(CommandExecutionRecord.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Count total executions matching the filter criteria.
     */
    public long count(
            UUID agentInstanceId,
            UUID templateId,
            ExecutionStatus status,
            String executedBy,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (agentInstanceId != null) {
            queryBuilder.append(" and agentInstance.id = :agentInstanceId");
            params.put("agentInstanceId", agentInstanceId);
        }
        if (templateId != null) {
            queryBuilder.append(" and template.id = :templateId");
            params.put("templateId", templateId);
        }
        if (status != null) {
            queryBuilder.append(" and status = :status");
            params.put("status", status);
        }
        if (executedBy != null && !executedBy.isBlank()) {
            queryBuilder.append(" and executedBy = :executedBy");
            params.put("executedBy", executedBy);
        }
        if (startTime != null) {
            queryBuilder.append(" and createdAt >= :startTime");
            params.put("startTime", startTime);
        }
        if (endTime != null) {
            queryBuilder.append(" and createdAt <= :endTime");
            params.put("endTime", endTime);
        }

        return CommandExecution.count(queryBuilder.toString(), params);
    }

    /**
     * Handle execution result from agent.
     */
    @Transactional
    public void handleExecutionResult(UUID executionId, String status, Integer exitCode, Long durationMs, String output) {
        CommandExecution execution = CommandExecution.findById(executionId);
        if (execution == null) {
            Log.warnf("Received result for unknown execution: %s", executionId);
            return;
        }

        ExecutionStatus newStatus = mapStatus(status);
        execution.status = newStatus;
        execution.exitCode = exitCode;
        execution.output = output;

        if (durationMs != null && execution.startedAt != null) {
            // Duration is already tracked via startedAt and finishedAt
        }

        if (newStatus == ExecutionStatus.SUCCESS || newStatus == ExecutionStatus.FAILED ||
            newStatus == ExecutionStatus.TIMEOUT || newStatus == ExecutionStatus.CANCELLED) {
            execution.finishedAt = LocalDateTime.now();
        }

        if (newStatus == ExecutionStatus.FAILED && output != null && output.contains("error")) {
            execution.errorMessage = output;
        }

        execution.persist();
        Log.infof("Execution %s completed with status=%s exitCode=%d",
                executionId, status, exitCode != null ? exitCode : -1);
    }

    /**
     * Send command to agent via WebSocket.
     */
    private boolean sendCommandToAgent(UUID hostId, UUID executionId, String command, Long timeout) {
        try {
            Map<String, Object> execMessage = new HashMap<>();
            execMessage.put("type", "EXEC");
            execMessage.put("requestId", executionId.toString());
            execMessage.put("content", Map.of(
                    "script", command,
                    "timeout", timeout != null ? timeout : DEFAULT_TIMEOUT
            ));

            String message = objectMapper.writeValueAsString(execMessage);
            return connectionManager.send(hostId, message);
        } catch (Exception e) {
            Log.errorf(e, "Error preparing command message for execution %s", executionId);
            return false;
        }
    }

    /**
     * Substitute parameters in command script.
     */
    private String substituteParameters(String script, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return script;
        }

        String result = script;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * Map agent status string to ExecutionStatus enum.
     */
    private ExecutionStatus mapStatus(String status) {
        if (status == null) {
            return ExecutionStatus.FAILED;
        }
        switch (status.toUpperCase()) {
            case "SUCCESS":
                return ExecutionStatus.SUCCESS;
            case "FAILED":
                return ExecutionStatus.FAILED;
            case "TIMEOUT":
                return ExecutionStatus.TIMEOUT;
            case "CANCELLED":
                return ExecutionStatus.CANCELLED;
            case "RUNNING":
                return ExecutionStatus.RUNNING;
            default:
                return ExecutionStatus.FAILED;
        }
    }

    /**
     * Record audit log.
     */
    private void recordAuditLog(String username, UUID userId, AuditAction action, AuditResult result,
                                String description, String resourceType, UUID resourceId,
                                String clientIp, String requestPath) {
        try {
            auditLogService.log(username, userId, action, result, description, resourceType, resourceId, clientIp, requestPath);
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}