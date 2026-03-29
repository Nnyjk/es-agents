package com.easystation.agent.collaboration.dto;

/**
 * 创建任务请求
 */
public class CreateTaskRequest {

    public String title;
    public String description;
    public String taskType;
    public String priority;
    public String assignedAgentId;
    public String parameters;

    public boolean validate() {
        return title != null && !title.trim().isEmpty();
    }
}
