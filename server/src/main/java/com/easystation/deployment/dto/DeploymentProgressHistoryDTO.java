package com.easystation.deployment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署进展历史 DTO
 */
@Data
public class DeploymentProgressHistoryDTO {
    public UUID id;
    public UUID deploymentId;
    public String stage;
    public String oldStatus;
    public String newStatus;
    public String message;
    public LocalDateTime createdAt;
}