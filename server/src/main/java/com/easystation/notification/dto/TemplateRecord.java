package com.easystation.notification.dto;

import com.easystation.notification.enums.ChannelType;
import com.easystation.notification.enums.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record TemplateRecord(
    UUID id,
    String name,
    TemplateType type,
    ChannelType channelType,
    String content,
    String variables,
    UUID createdBy,
    LocalDateTime createdAt
) {
    public record Create(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotNull(message = "Type cannot be null")
        TemplateType type,
        @NotNull(message = "Channel type cannot be null")
        ChannelType channelType,
        @NotBlank(message = "Content cannot be blank")
        String content,
        String variables
    ) {}

    public record Update(
        String name,
        TemplateType type,
        ChannelType channelType,
        String content,
        String variables
    ) {}
}