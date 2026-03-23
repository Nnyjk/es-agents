package com.easystation.plugin.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PluginCategoryRecord(
    UUID id,
    UUID parentId,
    String name,
    String code,
    String icon,
    String description,
    Integer sortOrder,
    Boolean isActive,
    Long pluginCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<PluginCategoryRecord> children
) {
    public record Create(
        UUID parentId,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name too long")
        String name,

        @Size(max = 50, message = "Code too long")
        String code,

        @Size(max = 500, message = "Icon URL too long")
        String icon,

        @Size(max = 500, message = "Description too long")
        String description,

        Integer sortOrder
    ) {}

    public record Update(
        @Size(max = 100, message = "Name too long")
        String name,

        @Size(max = 50, message = "Code too long")
        String code,

        @Size(max = 500, message = "Icon URL too long")
        String icon,

        @Size(max = 500, message = "Description too long")
        String description,

        Integer sortOrder,

        Boolean isActive
    ) {}

    public record Tree(
        UUID id,
        String name,
        String code,
        String icon,
        Long pluginCount,
        List<Tree> children
    ) {}
}