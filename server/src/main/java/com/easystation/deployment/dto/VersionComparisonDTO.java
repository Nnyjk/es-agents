package com.easystation.deployment.dto;

import lombok.Data;

import java.util.List;

/**
 * 版本对比 DTO
 */
@Data
public class VersionComparisonDTO {
    public DeploymentVersionDTO fromVersion;
    public DeploymentVersionDTO toVersion;
    public List<ChangeComparisonDTO> codeChanges;
    public List<ChangeComparisonDTO> configChanges;
    public List<ChangeComparisonDTO> dependencyChanges;
    public String summary;
    public Integer totalChanges;
    public Integer riskLevel;
    
    @Data
    public static class ChangeComparisonDTO {
        public String changeType;
        public String key;
        public String oldValue;
        public String newValue;
        public String description;
        public Integer riskLevel;
    }
}