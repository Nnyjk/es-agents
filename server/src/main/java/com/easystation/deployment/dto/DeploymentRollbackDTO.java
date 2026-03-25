package com.easystation.deployment.dto;

import com.easystation.deployment.enums.RollbackStatus;
import com.easystation.deployment.enums.RollbackStrategy;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署回滚 DTO
 */
@Data
public class DeploymentRollbackDTO {
    public UUID id;
    public String rollbackId;
    public UUID applicationId;
    public UUID environmentId;
    public UUID fromVersionId;
    public UUID toVersionId;
    public String fromVersion;
    public String toVersion;
    public RollbackStrategy strategy;
    public RollbackStatus status;
    public String reason;
    public String precheckResult;
    public LocalDateTime precheckAt;
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
    public Long duration;
    public String logs;
    public String verifyResult;
    public Boolean verifyPassed;
    public String notifyConfig;
    public Integer timeoutConfig;
    public Integer retryCount;
    public Integer maxRetry;
    public String triggeredBy;
    public LocalDateTime triggeredAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String createdBy;
    public String updatedBy;
}