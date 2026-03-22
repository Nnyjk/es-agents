package com.easystation.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgentTemplateVersionRecord(
    UUID id,
    UUID templateId,
    String templateName,
    String version,
    String description,
    String installScript,
    String configTemplate,
    String dependencies,
    String osType,
    String archSupport,
    boolean published,
    boolean latest,
    LocalDateTime createdAt,
    LocalDateTime publishedAt,
    String createdBy
) {
    public record Create(
        @NotNull
        UUID templateId,
        @NotBlank
        String version,
        String description,
        String installScript,
        String configTemplate,
        String dependencies,
        String osType,
        String archSupport
    ) {}

    public record Update(
        String description,
        String installScript,
        String configTemplate,
        String dependencies,
        String osType,
        String archSupport
    ) {}

    public record Publish(
        String description
    ) {}
}