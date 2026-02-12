package com.easystation.agent.record;

import com.easystation.agent.domain.enums.AgentCredentialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgentCredentialRecord(
    UUID id,
    String name,
    AgentCredentialType type,
    String config,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Simple(
        UUID id,
        String name,
        AgentCredentialType type
    ) {}

    public record Create(
        @NotBlank
        String name,
        @NotNull
        AgentCredentialType type,
        String config
    ) {}

    public record Update(
        String name,
        AgentCredentialType type,
        String config
    ) {}
}
