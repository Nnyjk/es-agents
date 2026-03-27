package com.easystation.agent.dto;

import java.time.Instant;
import java.util.List;

public record PluginInfoDTO(
    String id,
    String name,
    String version,
    String description,
    List<PluginCapabilityDTO> capabilities,
    String status, // RUNNING, STOPPED, ERROR
    Instant registeredAt,
    Instant lastSeen
) {}