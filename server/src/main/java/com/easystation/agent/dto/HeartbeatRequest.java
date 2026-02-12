package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record HeartbeatRequest(
    UUID agentId,
    AgentStatus status,
    LocalDateTime timestamp,
    String version,
    String osType
) {}
