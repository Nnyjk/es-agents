package com.easystation.deployment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ApplicationEnvironmentDTO {
    public UUID environmentId;
    public String environmentName;
    public String deployedVersion;
    public String status;
    public String lastDeployedAt;
    public String lastDeployedBy;
}