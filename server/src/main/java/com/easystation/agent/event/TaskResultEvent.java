package com.easystation.agent.event;

import java.util.UUID;

/**
 * Event fired when a task execution result is received from an agent.
 */
public class TaskResultEvent {
    
    private final UUID taskId;
    private final String status;
    private final Integer exitCode;
    private final Long durationMs;
    private final String output;
    
    public TaskResultEvent(UUID taskId, String status, Integer exitCode, Long durationMs, String output) {
        this.taskId = taskId;
        this.status = status;
        this.exitCode = exitCode;
        this.durationMs = durationMs;
        this.output = output;
    }
    
    public UUID getTaskId() {
        return taskId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Integer getExitCode() {
        return exitCode;
    }
    
    public Long getDurationMs() {
        return durationMs;
    }
    
    public String getOutput() {
        return output;
    }
}