package com.easystation.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ApiKeyRecord {

    public record Create(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 500) String description,
            LocalDateTime expiresAt,
            List<String> permissions,
            List<String> ipWhitelist,
            UUID createdBy
    ) {}

    public record Update(
            @Size(max = 255) String name,
            @Size(max = 500) String description,
            LocalDateTime expiresAt,
            Boolean enabled,
            List<String> permissions,
            List<String> ipWhitelist
    ) {}

    public record Detail(
            UUID id,
            String key,
            String name,
            String description,
            LocalDateTime expiresAt,
            boolean enabled,
            List<String> permissions,
            List<String> ipWhitelist,
            UUID createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime revokedAt,
            UUID revokedBy,
            String revokeReason,
            boolean expired,
            boolean valid,
            boolean revoked
    ) {}

    public record DetailWithoutKey(
            UUID id,
            String name,
            String description,
            LocalDateTime expiresAt,
            boolean enabled,
            List<String> permissions,
            List<String> ipWhitelist,
            UUID createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastUsedAt,
            LocalDateTime revokedAt,
            UUID revokedBy,
            String revokeReason,
            boolean expired,
            boolean valid,
            boolean revoked
    ) {}

    public record Query(
            String keyword,
            UUID createdBy,
            Boolean enabled,
            Boolean expired,
            Boolean revoked,
            Integer limit,
            Integer offset
    ) {}

    public record RevokeRequest(
            UUID revokedBy,
            String reason
    ) {}

    public record RefreshRequest(
            UUID updatedBy
    ) {}

    public record ValidationResult(
            boolean valid,
            UUID keyId,
            String keyName,
            List<String> permissions,
            String message
    ) {}

    public record UsageLogDetail(
            UUID id,
            UUID keyId,
            LocalDateTime usageTime,
            String clientIp,
            String requestMethod,
            String requestPath,
            Integer responseStatus,
            Long responseTimeMs,
            String permissionUsed,
            String errorMessage,
            LocalDateTime createdAt
    ) {}

    public record UsageLogQuery(
            UUID keyId,
            String clientIp,
            String requestMethod,
            String requestPath,
            Integer responseStatus,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit,
            Integer offset
    ) {}
}