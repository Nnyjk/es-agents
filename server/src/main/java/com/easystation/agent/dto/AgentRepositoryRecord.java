package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentRepositoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgentRepositoryRecord(
    UUID id,
    String name,
    AgentRepositoryType type,
    String baseUrl,
    String projectPath,
    String defaultBranch,
    AgentCredentialRecord.Simple credential,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Simple(
        UUID id,
        String name,
        AgentRepositoryType type
    ) {}

    public record Create(
        @NotBlank
        String name,
        @NotNull
        AgentRepositoryType type,
        @NotBlank
        String baseUrl,
        @NotBlank
        String projectPath,
        String defaultBranch,
        UUID credentialId
    ) {}

    public record Update(
        String name,
        AgentRepositoryType type,
        String baseUrl,
        String projectPath,
        String defaultBranch,
        UUID credentialId
    ) {}
}
