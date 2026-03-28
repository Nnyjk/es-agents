package com.easystation.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for batch upgrade operation.
 */
public record BatchUpgradeRequest(
    @NotEmpty(message = "Agent IDs cannot be empty")
    List<UUID> agentIds,

    @NotBlank(message = "Version cannot be empty")
    String version
) {}