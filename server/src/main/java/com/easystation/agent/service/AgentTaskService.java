package com.easystation.agent.service;

import com.easystation.agent.domain.AgentCommand;
import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.AgentTask;
import com.easystation.agent.domain.enums.AgentTaskStatus;
import com.easystation.agent.record.AgentTaskRecord;
import com.easystation.infra.socket.AgentConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AgentTaskService {

    @Inject
    AgentConnectionManager connectionManager;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Create and execute a task on an agent instance.
     */
    @Transactional
    public AgentTaskRecord execute(UUID agentInstanceId, UUID commandId, String args, String username) {
        AgentInstance instance = AgentInstance.findById(agentInstanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        AgentCommand command = AgentCommand.findById(commandId);
        if (command == null) {
            throw new WebApplicationException("Command not found", Response.Status.NOT_FOUND);
        }

        if (instance.host == null) {
            throw new WebApplicationException("Agent instance has no associated host", Response.Status.BAD_REQUEST);
        }

        // Create task
        AgentTask task = new AgentTask();
        task.agentInstance = instance;
        task.command = command;
        task.args = args;
        task.status = AgentTaskStatus.PENDING;
        task.persist();

        // Build and send execution message
        String script = command.script;
        if (args != null && !args.isBlank()) {
            script = substituteArgs(script, args);
        }

        try {
            Map<String, Object> execMessage = new HashMap<>();
            execMessage.put("type", "EXEC");
            execMessage.put("requestId", task.id.toString());
            execMessage.put("content", Map.of(
                    "script", script,
                    "timeout", command.timeout != null ? command.timeout : 300
            ));

            String message = objectMapper.writeValueAsString(execMessage);
            boolean sent = connectionManager.send(instance.host.id, message);

            if (sent) {
                task.status = AgentTaskStatus.SENT;
                Log.infof("Task %s sent to agent %s", task.id, agentInstanceId);
            } else {
                task.status = AgentTaskStatus.FAILED;
                task.result = "Failed to send command to agent: no active connection";
                Log.errorf("Failed to send task %s to agent %s", task.id, agentInstanceId);
            }
        } catch (Exception e) {
            task.status = AgentTaskStatus.FAILED;
            task.result = "Error preparing command: " + e.getMessage();
            Log.errorf(e, "Error sending task %s", task.id);
        }

        task.persist();
        return toRecord(task);
    }

    /**
     * Execute a script directly on an agent instance.
     */
    @Transactional
    public AgentTaskRecord executeScript(UUID agentInstanceId, String script, Long timeout, String username) {
        AgentInstance instance = AgentInstance.findById(agentInstanceId);
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        if (instance.host == null) {
            throw new WebApplicationException("Agent instance has no associated host", Response.Status.BAD_REQUEST);
        }

        // Create task without command reference
        AgentTask task = new AgentTask();
        task.agentInstance = instance;
        task.command = null; // Direct script execution
        task.args = script;
        task.status = AgentTaskStatus.PENDING;
        task.persist();

        try {
            Map<String, Object> execMessage = new HashMap<>();
            execMessage.put("type", "EXEC");
            execMessage.put("requestId", task.id.toString());
            execMessage.put("content", Map.of(
                    "script", script,
                    "timeout", timeout != null ? timeout : 300
            ));

            String message = objectMapper.writeValueAsString(execMessage);
            boolean sent = connectionManager.send(instance.host.id, message);

            if (sent) {
                task.status = AgentTaskStatus.SENT;
                Log.infof("Direct task %s sent to agent %s", task.id, agentInstanceId);
            } else {
                task.status = AgentTaskStatus.FAILED;
                task.result = "Failed to send command to agent: no active connection";
            }
        } catch (Exception e) {
            task.status = AgentTaskStatus.FAILED;
            task.result = "Error preparing command: " + e.getMessage();
            Log.errorf(e, "Error sending direct task %s", task.id);
        }

        task.persist();
        return toRecord(task);
    }

    /**
     * Handle execution result from agent.
     */
    @Transactional
    public void handleExecutionResult(UUID taskId, String status, Integer exitCode, Long durationMs, String output) {
        AgentTask task = AgentTask.findById(taskId);
        if (task == null) {
            Log.warnf("Received result for unknown task: %s", taskId);
            return;
        }

        task.status = "SUCCESS".equals(status) ? AgentTaskStatus.SUCCESS : AgentTaskStatus.FAILED;
        task.exitCode = exitCode;
        task.durationMs = durationMs;
        task.result = output;
        task.persist();

        Log.infof("Task %s completed with status=%s exitCode=%d durationMs=%d", 
                taskId, status, exitCode != null ? exitCode : -1, durationMs != null ? durationMs : -1);
    }

    /**
     * Retry a failed task.
     */
    @Transactional
    public AgentTaskRecord retry(UUID taskId, String username) {
        AgentTask original = AgentTask.findById(taskId);
        if (original == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }

        if (original.status != AgentTaskStatus.FAILED) {
            throw new WebApplicationException("Only failed tasks can be retried", Response.Status.BAD_REQUEST);
        }

        // Create new task based on original
        AgentTask retry = new AgentTask();
        retry.agentInstance = original.agentInstance;
        retry.command = original.command;
        retry.args = original.args;
        retry.status = AgentTaskStatus.PENDING;
        retry.persist();

        // Resend command
        if (original.command != null) {
            return execute(original.agentInstance.id, original.command.id, original.args, username);
        } else {
            return executeScript(original.agentInstance.id, original.args, 300L, username);
        }
    }

    /**
     * Get task by ID.
     */
    public AgentTaskRecord get(UUID taskId) {
        AgentTask task = AgentTask.findById(taskId);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }
        return toRecord(task);
    }

    /**
     * List tasks by agent instance.
     */
    public List<AgentTaskRecord> listByAgentInstance(UUID agentInstanceId, AgentTaskStatus status, int limit) {
        List<AgentTask> tasks;
        if (status != null) {
            tasks = AgentTask.find("agentInstance.id = ?1 and status = ?2 order by createdAt desc", 
                    agentInstanceId, status).page(0, limit).list();
        } else {
            tasks = AgentTask.find("agentInstance.id = ?1 order by createdAt desc", 
                    agentInstanceId).page(0, limit).list();
        }

        return tasks.stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    /**
     * List recent tasks.
     */
    public List<AgentTaskRecord> listRecent(int limit) {
        List<AgentTask> tasks = AgentTask.find("order by createdAt desc").page(0, limit).list();
        return tasks.stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a pending task.
     */
    @Transactional
    public AgentTaskRecord cancel(UUID taskId) {
        AgentTask task = AgentTask.findById(taskId);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }

        if (task.status != AgentTaskStatus.PENDING && task.status != AgentTaskStatus.SENT) {
            throw new WebApplicationException("Only pending or sent tasks can be cancelled", Response.Status.BAD_REQUEST);
        }

        task.status = AgentTaskStatus.FAILED;
        task.result = "Cancelled by user";
        task.persist();

        Log.infof("Task %s cancelled", taskId);
        return toRecord(task);
    }

    /**
     * Delete a task.
     */
    @Transactional
    public void delete(UUID taskId) {
        AgentTask task = AgentTask.findById(taskId);
        if (task == null) {
            throw new WebApplicationException("Task not found", Response.Status.NOT_FOUND);
        }

        task.delete();
        Log.infof("Task %s deleted", taskId);
    }

    /**
     * Count tasks by status.
     */
    public Map<AgentTaskStatus, Long> countByStatus() {
        Map<AgentTaskStatus, Long> counts = new EnumMap<>(AgentTaskStatus.class);
        for (AgentTaskStatus status : AgentTaskStatus.values()) {
            counts.put(status, AgentTask.count("status", status));
        }
        return counts;
    }

    private String substituteArgs(String script, String args) {
        if (args == null || args.isBlank()) {
            return script;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(args, Map.class);
            String result = script;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = String.valueOf(entry.getValue());
                result = result.replace(placeholder, value);
            }
            return result;
        } catch (Exception e) {
            Log.warnf("Failed to parse args: %s", e.getMessage());
            return script;
        }
    }

    private AgentTaskRecord toRecord(AgentTask task) {
        return new AgentTaskRecord(
                task.id,
                task.agentInstance != null ? task.agentInstance.id : null,
                task.agentInstance != null && task.agentInstance.host != null 
                        ? task.agentInstance.host.name : null,
                task.command != null ? task.command.name : "Direct Script",
                task.args,
                task.result,
                task.status,
                task.durationMs,
                task.createdAt,
                task.updatedAt
        );
    }
}