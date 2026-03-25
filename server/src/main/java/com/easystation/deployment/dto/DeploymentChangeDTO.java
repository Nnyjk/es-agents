package com.easystation.deployment.dto;

import com.easystation.deployment.enums.ChangeType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 部署变更 DTO
 */
@Data
public class DeploymentChangeDTO {
    public UUID id;
    public UUID versionId;
    public UUID applicationId;
    public ChangeType changeType;
    public String changeKey;
    public String changeTitle;
    public String oldValue;
    public String newValue;
    public String changeDiff;
    public String description;
    public Integer impactLevel;
    public String impactScope;
    public Integer riskLevel;
    public String riskDescription;
    public String commitHash;
    public String commitUrl;
    public String author;
    public String authorEmail;
    public String relatedIssue;
    public String relatedPr;
    public LocalDateTime createdAt;
    public String createdBy;
}