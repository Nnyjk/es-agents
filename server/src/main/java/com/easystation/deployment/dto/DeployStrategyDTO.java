package com.easystation.deployment.dto;

import com.easystation.deployment.domain.DeployStrategy;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署策略 DTO
 */
@Data
public class DeployStrategyDTO {
    public UUID id;
    public UUID applicationId;
    public UUID environmentId;
    public String name;
    public DeployStrategy.StrategyType type;
    public String deployConfig;
    public String healthCheckConfig;
    public String rollbackConfig;
    public String description;
    public Boolean active;
    public Boolean isDefault;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}