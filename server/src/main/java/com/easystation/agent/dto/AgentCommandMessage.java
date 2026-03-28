package com.easystation.agent.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Message sent to agent for command execution.
 */
public class AgentCommandMessage {

    private String type = "EXECUTE_COMMAND";
    private UUID executionId;
    private String command;
    private Map<String, Object> parameters;
    private Long timeout;

    public AgentCommandMessage() {
    }

    public AgentCommandMessage(UUID executionId, String command, Map<String, Object> parameters, Long timeout) {
        this.executionId = executionId;
        this.command = command;
        this.parameters = parameters;
        this.timeout = timeout != null ? timeout : 300L;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public void setExecutionId(UUID executionId) {
        this.executionId = executionId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}