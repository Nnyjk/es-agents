package com.easystation.agent.record;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AgentCommandRecord(
    UUID id,
    String name,
    String script,
    Long timeout,
    String defaultArgs,
    UUID templateId
) {
    public record Create(
        @NotBlank
        String name,
        String script,
        Long timeout,
        String defaultArgs,
        UUID templateId
    ) {}

    public record Update(
        String name,
        String script,
        Long timeout,
        String defaultArgs
    ) {}
}
