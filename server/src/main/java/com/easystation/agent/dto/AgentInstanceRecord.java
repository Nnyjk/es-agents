package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgentInstanceRecord(
    UUID id,
    UUID hostId,
    String hostName,
    UUID templateId,
    String templateName,
    AgentStatus status,
    String version,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotNull
        UUID hostId,
        @NotNull
        UUID templateId
    ) {}

    public record Update(
        UUID hostId,
        UUID templateId
    ) {}

    public record ExecuteCommand(
        @NotNull
        UUID commandId,
        String args
    ) {}
}
