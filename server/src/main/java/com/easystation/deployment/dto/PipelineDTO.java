package com.easystation.deployment.dto;

import com.easystation.deployment.enums.PipelineStatus;
import com.easystation.deployment.enums.PipelineType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PipelineDTO {
    public UUID id;
    public String name;
    public UUID applicationId;
    public String applicationName;
    public PipelineType pipelineType;
    public String description;
    public List<PipelineStageDTO> stages;
    public PipelineStatus status;
    public String triggerType;
    public String cronExpression;
    public LocalDateTime lastExecutionAt;
    public String lastExecutionStatus;
    public String createdBy;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}