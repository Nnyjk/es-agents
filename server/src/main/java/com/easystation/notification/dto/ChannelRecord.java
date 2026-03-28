package com.easystation.notification.dto;

import com.easystation.notification.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChannelRecord(
    UUID id,
    String name,
    ChannelType type,
    String config,
    Boolean enabled,
    UUID createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record Create(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotNull(message = "Type cannot be null")
        ChannelType type,
        String config,
        Boolean enabled
    ) {}

    public record Update(
        String name,
        ChannelType type,
        String config,
        Boolean enabled
    ) {}

    public record TestRequest(
        @NotBlank(message = "Recipient cannot be blank")
        String recipient,
        String title,
        String content
    ) {}

    public record TestResult(
        boolean success,
        String message
    ) {}
}