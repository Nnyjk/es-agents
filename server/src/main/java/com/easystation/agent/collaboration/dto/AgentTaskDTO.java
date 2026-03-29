package com.easystation.agent.collaboration.dto;

import com.easystation.agent.collaboration.domain.TaskStatus;
import java.time.LocalDateTime;

public class AgentTaskDTO {
    public Long id;
    public Long sessionId;
    public String title;
    public String description;
    public String taskType;
    public String priority;
    public TaskStatus status;
    public String assignedTo;
    public String createdBy;
    public String result;
    public String error;
    public LocalDateTime assignedAt;
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
