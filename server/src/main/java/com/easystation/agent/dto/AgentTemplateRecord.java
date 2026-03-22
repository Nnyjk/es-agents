package com.easystation.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgentTemplateRecord(
    UUID id,
    String name,
    String description,
    String category,
    String osType,
    String archSupport,
    String installScript,
    String configTemplate,
    String dependencies,
    AgentSourceRecord source,
    List<AgentCommandRecord> commands,
    Integer deploymentCount,
    Integer successCount,
    Double successRate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotBlank
        String name,
        String description,
        String category,
        String osType,
        String archSupport,
        String installScript,
        String configTemplate,
        String dependencies,
        @NotNull
        UUID sourceId,
        List<AgentCommandRecord.Create> commands
    ) {}

    public record Update(
        String name,
        String description,
        String category,
        String osType,
        String archSupport,
        String installScript,
        String configTemplate,
        String dependencies,
        UUID sourceId
    ) {}

    public record ImportData(
        String name,
        String description,
        String category,
        String osType,
        String archSupport,
        String installScript,
        String configTemplate,
        String dependencies,
        List<AgentCommandRecord.Create> commands
    ) {}

    public record Statistics(
        UUID templateId,
        String templateName,
        Integer deploymentCount,
        Integer successCount,
        Integer failureCount,
        Double successRate,
        Integer instanceCount
    ) {}
}