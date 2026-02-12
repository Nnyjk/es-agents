package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgentSourceRecord(
    UUID id,
    String name,
    AgentSourceType type,
    String config,
    AgentRepositoryRecord.Simple repository,
    AgentCredentialRecord.Simple credential,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotBlank
        String name,
        @NotNull
        AgentSourceType type,
        String config,
        UUID repositoryId,
        UUID credentialId
    ) {}

    public record Update(
        String name,
        AgentSourceType type,
        String config,
        UUID repositoryId,
        UUID credentialId
    ) {}
}
