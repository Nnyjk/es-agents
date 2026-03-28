package com.easystation.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for batch command execution.
 */
public record BatchCommandRequest(
    @NotEmpty(message = "Host IDs cannot be empty")
    List<UUID> hostIds,

    @NotBlank(message = "Command cannot be empty")
    String command
) {}