package com.easystation.agent.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agent 健康度记录
 */
public record AgentHealthRecord(
    UUID id,
    String agentName,
    HealthStatus status,
    String statusMessage,
    LocalDateTime lastHeartbeatTime,
    Long heartbeatAgeSeconds,
    Long successfulTasks24h,
    Long failedTasks24h,
    Long totalTasks24h,
    Double successRate24h,
    LocalDateTime lastSuccessfulTaskTime,
    LocalDateTime lastFailedTaskTime,
    List<String> issues
) {
    public enum HealthStatus {
        HEALTHY("健康"),
        WARNING("警告"),
        CRITICAL("严重"),
        UNKNOWN("未知");

        private final String displayName;

        HealthStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 构建健康度记录的 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String agentName;
        private HealthStatus status = HealthStatus.UNKNOWN;
        private String statusMessage;
        private LocalDateTime lastHeartbeatTime;
        private Long heartbeatAgeSeconds;
        private Long successfulTasks24h = 0L;
        private Long failedTasks24h = 0L;
        private Long totalTasks24h = 0L;
        private Double successRate24h;
        private LocalDateTime lastSuccessfulTaskTime;
        private LocalDateTime lastFailedTaskTime;
        private final List<String> issues = new ArrayList<>();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public Builder status(HealthStatus status) {
            this.status = status;
            return this;
        }

        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder lastHeartbeatTime(LocalDateTime lastHeartbeatTime) {
            this.lastHeartbeatTime = lastHeartbeatTime;
            return this;
        }

        public Builder heartbeatAgeSeconds(Long heartbeatAgeSeconds) {
            this.heartbeatAgeSeconds = heartbeatAgeSeconds;
            return this;
        }

        public Builder successfulTasks24h(Long successfulTasks24h) {
            this.successfulTasks24h = successfulTasks24h;
            return this;
        }

        public Builder failedTasks24h(Long failedTasks24h) {
            this.failedTasks24h = failedTasks24h;
            return this;
        }

        public Builder totalTasks24h(Long totalTasks24h) {
            this.totalTasks24h = totalTasks24h;
            return this;
        }

        public Builder successRate24h(Double successRate24h) {
            this.successRate24h = successRate24h;
            return this;
        }

        public Builder lastSuccessfulTaskTime(LocalDateTime lastSuccessfulTaskTime) {
            this.lastSuccessfulTaskTime = lastSuccessfulTaskTime;
            return this;
        }

        public Builder lastFailedTaskTime(LocalDateTime lastFailedTaskTime) {
            this.lastFailedTaskTime = lastFailedTaskTime;
            return this;
        }

        public Builder addIssue(String issue) {
            this.issues.add(issue);
            return this;
        }

        public Builder issues(List<String> issues) {
            this.issues.addAll(issues);
            return this;
        }

        public AgentHealthRecord build() {
            if (successRate24h == null && totalTasks24h != null && totalTasks24h > 0) {
                successRate24h = (double) successfulTasks24h / totalTasks24h * 100;
            } else if (successRate24h == null) {
                successRate24h = 0.0;
            }
            
            if (statusMessage == null) {
                statusMessage = status.getDisplayName();
            }
            
            return new AgentHealthRecord(
                id, agentName, status, statusMessage,
                lastHeartbeatTime, heartbeatAgeSeconds,
                successfulTasks24h, failedTasks24h, totalTasks24h, successRate24h,
                lastSuccessfulTaskTime, lastFailedTaskTime,
                List.copyOf(issues)
            );
        }
    }
}