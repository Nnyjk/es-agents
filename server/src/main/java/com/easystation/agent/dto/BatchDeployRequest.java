package com.easystation.agent.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for batch deploy operation.
 */
public record BatchDeployRequest(
    @NotEmpty(message = "Agent IDs cannot be empty")
    List<UUID> agentIds
) {}