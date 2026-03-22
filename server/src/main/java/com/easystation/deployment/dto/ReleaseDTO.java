package com.easystation.deployment.dto;

import com.easystation.deployment.enums.ReleaseStatus;
import com.easystation.deployment.enums.ReleaseType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ReleaseDTO {
    public UUID id;
    public String releaseId;
    public UUID applicationId;
    public String applicationName;
    public UUID environmentId;
    public String environmentName;
    public String version;
    public ReleaseType type;
    public ReleaseStatus status;
    public String changeLog;
    public String createdBy;
    public String approvedBy;
    public LocalDateTime approvedAt;
    public String deployedBy;
    public LocalDateTime deployedAt;
    public List<ReleaseStageDTO> stages;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}