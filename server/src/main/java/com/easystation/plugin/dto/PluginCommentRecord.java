package com.easystation.plugin.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PluginCommentRecord(
    UUID id,
    UUID pluginId,
    String pluginName,
    UUID userId,
    String userName,
    String userAvatar,
    UUID parentId,
    UUID replyToUserId,
    String replyToUserName,
    String content,
    Boolean isDeveloperReply,
    Boolean isPinned,
    Boolean isHidden,
    Integer likeCount,
    Integer replyCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<PluginCommentRecord> replies
) {
    public record Create(
        @NotNull(message = "Plugin ID is required")
        UUID pluginId,

        UUID parentId,

        UUID replyToUserId,

        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content too long")
        String content
    ) {}

    public record Update(
        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content too long")
        String content
    ) {}

    public record Query(
        UUID pluginId,
        UUID userId,
        Boolean isDeveloperReply,
        Boolean includeHidden,
        String sortBy,
        String sortOrder,
        Integer page,
        Integer size
    ) {}
}