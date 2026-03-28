package com.easystation.deployment.dto;

import com.easystation.deployment.enums.ProgressStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署进展 DTO
 */
@Data
public class DeploymentProgressDTO {
    public UUID id;
    public UUID deploymentId;
    public String stage;
    public ProgressStatus status;
    public Integer progressPercent;
    public String message;
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}