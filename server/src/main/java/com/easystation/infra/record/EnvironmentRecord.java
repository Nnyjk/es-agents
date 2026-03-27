package com.easystation.infra.record;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnvironmentRecord(
    UUID id,
    String name,
    String code,
    String description,
    Boolean enabled,
    String color,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Code cannot be blank")
        String code,
        String description,
        String color
    ) {}

    public record Update(
        String description,
        Boolean enabled,
        String color
    ) {}
}
