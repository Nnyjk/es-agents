package com.easystation.profile.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionRecord(
    UUID id,
    String deviceInfo,
    String ipAddress,
    String userAgent,
    LocalDateTime loginAt,
    LocalDateTime lastActivityAt,
    Boolean isCurrent
) {
    public record Summary(
        int totalCount,
        int activeCount
    ) {}
}