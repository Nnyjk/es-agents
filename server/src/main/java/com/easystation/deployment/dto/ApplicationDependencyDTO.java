package com.easystation.deployment.dto;

import com.easystation.deployment.domain.ApplicationDependency;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 应用依赖 DTO
 */
@Data
public class ApplicationDependencyDTO {
    public UUID id;
    public UUID applicationId;
    public UUID environmentId;
    public ApplicationDependency.DependencyType type;
    public String dependencyName;
    public String dependencyVersion;
    public String description;
    public Boolean active;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}