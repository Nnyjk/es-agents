package com.easystation.deployment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EnvironmentApplicationDTO {
    public UUID applicationId;
    public String applicationName;
    public String deployedVersion;
    public String status;
    public String deployedBy;
    public LocalDateTime deployedAt;
    public String healthStatus;
}