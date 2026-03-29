package com.easystation.agent.collaboration.dto;

/**
 * 任务分配请求
 */
public class TaskAssignmentRequest {

    public Long taskId;
    public String agentId;

    public boolean validate() {
        return taskId != null && agentId != null && !agentId.trim().isEmpty();
    }
}
