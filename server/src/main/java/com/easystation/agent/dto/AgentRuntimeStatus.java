package com.easystation.agent.dto;

import com.easystation.agent.domain.enums.AgentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agent 实时运行状态记录
 */
public record AgentRuntimeStatus(
    UUID id,
    AgentStatus status,
    String version,
    LocalDateTime lastHeartbeatTime,
    Long heartbeatAgeSeconds,
    boolean isOnline,
    String statusMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * 计算健康状态
     */
    public HealthStatus getHealthStatus() {
        if (status == AgentStatus.ERROR) {
            return HealthStatus.CRITICAL;
        }
        if (lastHeartbeatTime == null) {
            return HealthStatus.UNKNOWN;
        }
        if (heartbeatAgeSeconds == null || heartbeatAgeSeconds < 60) {
            return HealthStatus.HEALTHY;
        }
        if (heartbeatAgeSeconds < 300) { // 5 分钟
            return HealthStatus.WARNING;
        }
        return HealthStatus.CRITICAL;
    }

    public enum HealthStatus {
        HEALTHY,
        WARNING,
        CRITICAL,
        UNKNOWN
    }
}