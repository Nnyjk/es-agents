package com.easystation.notification.dto;

import com.easystation.notification.enums.NotificationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoryRecord(
    UUID id,
    UUID channelId,
    UUID templateId,
    String recipient,
    String title,
    String content,
    NotificationStatus status,
    LocalDateTime sentAt,
    String errorMessage,
    Integer retryCount,
    LocalDateTime createdAt
) {
    public record Query(
        UUID channelId,
        NotificationStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer limit,
        Integer offset
    ) {}
}