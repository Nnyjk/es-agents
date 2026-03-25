package com.easystation.deployment.dto;

import com.easystation.deployment.enums.VersionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署版本 DTO
 */
@Data
public class DeploymentVersionDTO {
    public UUID id;
    public String versionId;
    public UUID applicationId;
    public UUID environmentId;
    public UUID releaseId;
    public String version;
    public String commitHash;
    public String commitMessage;
    public String commitAuthor;
    public LocalDateTime commitTime;
    public Integer buildNumber;
    public String buildUrl;
    public String artifactUrl;
    public String artifactChecksum;
    public VersionStatus status;
    public String config;
    public String deployConfig;
    public String deployBy;
    public LocalDateTime deployAt;
    public Long deployDuration;
    public String rollbackBy;
    public LocalDateTime rollbackAt;
    public String notes;
    public Boolean isStable;
    public Boolean isProblematic;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String createdBy;
    public String updatedBy;
}