package com.easystation.agent.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgentTemplateRecord(
    UUID id,
    String name,
    String description,
    String osType,
    AgentSourceRecord source,
    List<AgentCommandRecord> commands,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotBlank
        String name,
        String description,
        String osType,
        @NotNull
        UUID sourceId,
        List<AgentCommandRecord.Create> commands
    ) {}

    public record Update(
        String name,
        String description,
        String osType,
        UUID sourceId
    ) {}
}
