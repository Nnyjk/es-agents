package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentTaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgentTaskRecord(
    UUID id,
    UUID agentInstanceId,
    String agentInstanceName,
    String commandName,
    String args,
    String result,
    AgentTaskStatus status,
    Long durationMs,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}