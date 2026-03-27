package com.easystation.agent.dto;

import java.time.Instant;
import java.util.List;

public record PluginCapabilityDTO(
    String name,
    String description,
    List<String> commands
) {}