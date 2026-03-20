package com.easystation.agent.service;

import com.easystation.agent.domain.AgentInstance;
import com.easystation.agent.domain.CommandExecution;
import com.easystation.agent.domain.CommandTemplate;
import com.easystation.agent.domain.enums.CommandCategory;
import com.easystation.agent.domain.enums.ExecutionStatus;
import com.easystation.agent.record.CommandExecutionRecord;
import com.easystation.agent.record.CommandTemplateRecord;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandTemplateService {

    /**
     * List all command templates with optional filtering.
     */
    public List<CommandTemplateRecord.ListResponse> list(CommandCategory category, Boolean activeOnly) {
        List<CommandTemplate> templates;
        if (category != null && activeOnly != null && activeOnly) {
            templates = CommandTemplate.list("category = ?1 and isActive = true", category);
        } else if (category != null) {
            templates = CommandTemplate.list("category", category);
        } else if (activeOnly != null && activeOnly) {
            templates = CommandTemplate.listActive();
        } else {
            templates = CommandTemplate.listAll();
        }

        return templates.stream()
                .map(CommandTemplateRecord.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get command template by ID.
     */
    public CommandTemplateRecord.DetailResponse getById(UUID id) {
        CommandTemplate template = CommandTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Command template not found", Response.Status.NOT_FOUND);
        }
        return CommandTemplateRecord.DetailResponse.from(template);
    }

    /**
     * Create a new command template.
     */
    @Transactional
    public CommandTemplateRecord.DetailResponse create(CommandTemplateRecord.CreateRequest request, String username) {
        // Check for duplicate name
        CommandTemplate existing = CommandTemplate.findByName(request.name());
        if (existing != null) {
            throw new WebApplicationException("Command template with this name already exists", 
                    Response.Status.CONFLICT);
        }

        CommandTemplate template = new CommandTemplate();
        template.name = request.name();
        template.description = request.description();
        template.script = request.script();
        template.category = request.category() != null ? request.category() : CommandCategory.CUSTOM;
        template.tags = request.tags();
        template.parameters = request.parameters();
        template.timeout = request.timeout() != null ? request.timeout() : 300L;
        template.retryCount = request.retryCount() != null ? request.retryCount() : 0;
        template.createdBy = username;
        template.persist();

        Log.infof("Created command template %s (%s)", template.id, template.name);
        return CommandTemplateRecord.DetailResponse.from(template);
    }

    /**
     * Update a command template.
     */
    @Transactional
    public CommandTemplateRecord.DetailResponse update(UUID id, CommandTemplateRecord.UpdateRequest request) {
        CommandTemplate template = CommandTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Command template not found", Response.Status.NOT_FOUND);
        }

        // Check for duplicate name if name is being changed
        if (request.name() != null && !request.name().equals(template.name)) {
            CommandTemplate existing = CommandTemplate.findByName(request.name());
            if (existing != null) {
                throw new WebApplicationException("Command template with this name already exists", 
                        Response.Status.CONFLICT);
            }
            template.name = request.name();
        }

        if (request.description() != null) template.description = request.description();
        if (request.script() != null) template.script = request.script();
        if (request.category() != null) template.category = request.category();
        if (request.tags() != null) template.tags = request.tags();
        if (request.parameters() != null) template.parameters = request.parameters();
        if (request.timeout() != null) template.timeout = request.timeout();
        if (request.retryCount() != null) template.retryCount = request.retryCount();
        if (request.isActive() != null) template.isActive = request.isActive();

        template.persist();
        Log.infof("Updated command template %s", template.id);
        return CommandTemplateRecord.DetailResponse.from(template);
    }

    /**
     * Delete a command template.
     */
    @Transactional
    public void delete(UUID id) {
        CommandTemplate template = CommandTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Command template not found", Response.Status.NOT_FOUND);
        }

        template.delete();
        Log.infof("Deleted command template %s", id);
    }

    /**
     * Execute a command template on an agent instance.
     */
    @Transactional
    public CommandTemplateRecord.ExecuteResponse execute(UUID id, CommandTemplateRecord.ExecuteRequest request, 
            String username) {
        CommandTemplate template = CommandTemplate.findById(id);
        if (template == null) {
            throw new WebApplicationException("Command template not found", Response.Status.NOT_FOUND);
        }

        if (!template.isActive) {
            throw new WebApplicationException("Command template is not active", Response.Status.BAD_REQUEST);
        }

        AgentInstance instance = AgentInstance.findById(request.agentInstanceId());
        if (instance == null) {
            throw new WebApplicationException("Agent instance not found", Response.Status.NOT_FOUND);
        }

        // Create execution record
        CommandExecution execution = new CommandExecution();
        execution.template = template;
        execution.agentInstance = instance;
        execution.command = substituteParameters(template.script, request.parameters());
        execution.parameters = request.parameters();
        execution.status = ExecutionStatus.PENDING;
        execution.executedBy = username;
        execution.startedAt = LocalDateTime.now();
        execution.persist();

        Log.infof("Created command execution %s from template %s", execution.id, template.id);

        // TODO: Actually send command to agent via WebSocket or message queue
        // For now, just create the execution record

        return new CommandTemplateRecord.ExecuteResponse(execution.id, 
                "Command execution created and pending");
    }

    /**
     * Substitute parameters in the command script.
     */
    private String substituteParameters(String script, String parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return script;
        }
        
        // Simple parameter substitution: replace ${param} with actual values
        // Parameters are expected to be in JSON format
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> params = mapper.readValue(parameters, java.util.Map.class);
            
            String result = script;
            for (java.util.Map.Entry<String, Object> entry : params.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = String.valueOf(entry.getValue());
                result = result.replace(placeholder, value);
            }
            return result;
        } catch (Exception e) {
            Log.warnf("Failed to parse parameters: %s", e.getMessage());
            return script;
        }
    }

    /**
     * Get execution history for a template.
     */
    public List<CommandExecutionRecord.ListResponse> getExecutionHistory(UUID templateId) {
        List<CommandExecution> executions = CommandExecution.findByTemplateId(templateId);
        return executions.stream()
                .map(CommandExecutionRecord.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get execution by ID.
     */
    public CommandExecutionRecord.DetailResponse getExecutionById(UUID executionId) {
        CommandExecution execution = CommandExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException("Command execution not found", Response.Status.NOT_FOUND);
        }
        return CommandExecutionRecord.DetailResponse.from(execution);
    }

    /**
     * Update execution status (for callback from agent).
     */
    @Transactional
    public CommandExecutionRecord.DetailResponse updateExecutionStatus(UUID executionId, ExecutionStatus status, 
            String output, Integer exitCode, String errorMessage) {
        CommandExecution execution = CommandExecution.findById(executionId);
        if (execution == null) {
            throw new WebApplicationException("Command execution not found", Response.Status.NOT_FOUND);
        }

        execution.status = status;
        if (output != null) execution.output = output;
        if (exitCode != null) execution.exitCode = exitCode;
        if (errorMessage != null) execution.errorMessage = errorMessage;
        
        if (status == ExecutionStatus.SUCCESS || status == ExecutionStatus.FAILED || 
            status == ExecutionStatus.TIMEOUT || status == ExecutionStatus.CANCELLED) {
            execution.finishedAt = LocalDateTime.now();
        }

        execution.persist();
        Log.infof("Updated execution %s status to %s", executionId, status);
        return CommandExecutionRecord.DetailResponse.from(execution);
    }
}