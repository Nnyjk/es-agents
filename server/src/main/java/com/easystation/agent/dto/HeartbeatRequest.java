package com.easystation.agent.dto;

import com.easystation.agent.domain.enums.AgentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HeartbeatRequest(
    UUID agentId,
    AgentStatus status,
    LocalDateTime timestamp,
    String version,
    String osType,
    Double cpuUsage,
    Double memoryUsage,
    Double diskUsage,
    List<PluginInfoDTO> plugins
) {}
