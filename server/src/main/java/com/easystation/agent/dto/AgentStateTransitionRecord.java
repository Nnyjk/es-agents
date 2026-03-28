package com.easystation.agent.dto;

import com.easystation.agent.domain.enums.AgentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Agent 状态流转相关 DTO
 */
public record AgentStateTransitionRecord(
    UUID id,
    UUID instanceId,
    AgentStatus fromStatus,
    AgentStatus toStatus,
    String reason,
    String operator,
    LocalDateTime transitionTime,
    LocalDateTime createdAt
) {
    /**
     * 状态流转请求
     */
    public record TransitionRequest(
        @NotNull
        AgentStatus targetStatus,
        String reason,
        String operator
    ) {}

    /**
     * 状态流转结果
     */
    public record TransitionResult(
        UUID instanceId,
        AgentStatus previousStatus,
        AgentStatus currentStatus,
        boolean success,
        String message,
        LocalDateTime transitionTime
    ) {}

    /**
     * 可用流转列表
     */
    public record AvailableTransitions(
        UUID instanceId,
        AgentStatus currentStatus,
        List<AgentStatus> availableTargets,
        String message
    ) {}

    /**
     * 状态变更历史记录
     */
    public record StateHistory(
        UUID instanceId,
        AgentStatus currentStatus,
        List<StateChangeEntry> history,
        int total,
        int page,
        int size
    ) {}

    /**
     * 单条状态变更记录
     */
    public record StateChangeEntry(
        UUID id,
        AgentStatus status,
        String version,
        Long heartbeatDelaySeconds,
        LocalDateTime snapshotTime,
        String extraInfo
    ) {}
}