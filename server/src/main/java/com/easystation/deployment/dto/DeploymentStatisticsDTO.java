package com.easystation.deployment.dto;

import java.util.List;

/**
 * 部署统计DTO
 */
public class DeploymentStatisticsDTO {
    
    private Long totalDeployments;
    private Long successCount;
    private Long failedCount;
    private Long rollbackCount;
    private Double successRate;
    private Double avgDuration;
    private List<FailureReasonDTO> failureReasons;
    private List<DeploymentTrendDTO> deploymentTrends;
    
    // Getters and Setters
    public Long getTotalDeployments() {
        return totalDeployments;
    }
    
    public void setTotalDeployments(Long totalDeployments) {
        this.totalDeployments = totalDeployments;
    }
    
    public Long getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }
    
    public Long getFailedCount() {
        return failedCount;
    }
    
    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }
    
    public Long getRollbackCount() {
        return rollbackCount;
    }
    
    public void setRollbackCount(Long rollbackCount) {
        this.rollbackCount = rollbackCount;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
    
    public Double getAvgDuration() {
        return avgDuration;
    }
    
    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }
    
    public List<FailureReasonDTO> getFailureReasons() {
        return failureReasons;
    }
    
    public void setFailureReasons(List<FailureReasonDTO> failureReasons) {
        this.failureReasons = failureReasons;
    }
    
    public List<DeploymentTrendDTO> getDeploymentTrends() {
        return deploymentTrends;
    }
    
    public void setDeploymentTrends(List<DeploymentTrendDTO> deploymentTrends) {
        this.deploymentTrends = deploymentTrends;
    }
    
    /**
     * 失败原因DTO
     */
    public static class FailureReasonDTO {
        private String reason;
        private Long count;
        private Double percentage;
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public Long getCount() {
            return count;
        }
        
        public void setCount(Long count) {
            this.count = count;
        }
        
        public Double getPercentage() {
            return percentage;
        }
        
        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }
    
    /**
     * 部署趋势DTO
     */
    public static class DeploymentTrendDTO {
        private String date;
        private Long count;
        private Double successRate;
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public Long getCount() {
            return count;
        }
        
        public void setCount(Long count) {
            this.count = count;
        }
        
        public Double getSuccessRate() {
            return successRate;
        }
        
        public void setSuccessRate(Double successRate) {
            this.successRate = successRate;
        }
    }
}