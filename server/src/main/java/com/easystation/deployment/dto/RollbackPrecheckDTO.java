package com.easystation.deployment.dto;

import lombok.Data;

import java.util.List;

/**
 * 回滚预检结果 DTO
 */
@Data
public class RollbackPrecheckDTO {
    public Boolean canRollback;
    public String fromVersion;
    public String toVersion;
    public List<String> warnings;
    public List<String> errors;
    public List<String> checklist;
    public ImpactAnalysisDTO impactAnalysis;
    
    @Data
    public static class ImpactAnalysisDTO {
        public Integer affectedServices;
        public Integer affectedInstances;
        public List<String> affectedComponents;
        public String estimatedTime;
        public String riskLevel;
    }
}