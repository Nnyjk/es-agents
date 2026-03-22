package com.easystation.deployment.dto;

import com.easystation.deployment.enums.PipelineStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PipelineExecutionDTO {
    public UUID id;
    public UUID pipelineId;
    public String pipelineName;
    public Integer executionNumber;
    public PipelineStatus status;
    public String triggeredBy;
    public String triggerType;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public String logs;
    public List<StageExecutionDTO> stages;
    public LocalDateTime createdAt;
}