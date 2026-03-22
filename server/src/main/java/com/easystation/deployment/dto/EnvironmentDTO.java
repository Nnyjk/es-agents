package com.easystation.deployment.dto;

import com.easystation.deployment.enums.EnvironmentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class EnvironmentDTO {
    public UUID id;
    public String name;
    public EnvironmentType environmentType;
    public String description;
    public String clusterConfig;
    public String resourceQuota;
    public List<String> permissions;
    public String configCenter;
    public Boolean active;
    public String createdBy;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}