package com.easystation.auth.dto;

import com.easystation.auth.domain.enums.TokenScope;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ApiTokenRecord {

    public record Create(
            @NotBlank String name,
            UUID userId,
            String description,
            TokenScope scope,
            LocalDateTime expiresAt,
            String createdBy
    ) {}

    public record Update(
            String name,
            String description,
            TokenScope scope,
            LocalDateTime expiresAt
    ) {}

    public record Detail(
            UUID id,
            String token,
            String name,
            UUID userId,
            String description,
            TokenScope scope,
            LocalDateTime expiresAt,
            LocalDateTime lastUsedAt,
            boolean revoked,
            LocalDateTime revokedAt,
            String revokedBy,
            String revokedReason,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean expired,
            boolean valid
    ) {}

    public record Query(
            String keyword,
            UUID userId,
            TokenScope scope,
            Boolean revoked,
            Boolean expired,
            Integer limit,
            Integer offset
    ) {}

    public record RevokeRequest(
            String revokedBy,
            String reason
    ) {}

    public record AccessLogDetail(
            UUID id,
            UUID tokenId,
            LocalDateTime accessTime,
            String clientIp,
            String requestMethod,
            String requestPath,
            Integer responseStatus,
            Long responseTimeMs,
            String errorMessage,
            LocalDateTime createdAt
    ) {}

    public record AccessLogQuery(
            UUID tokenId,
            String clientIp,
            String requestMethod,
            String requestPath,
            Integer responseStatus,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer limit,
            Integer offset
    ) {}

    public record TokenValidation(
            boolean valid,
            UUID tokenId,
            String tokenName,
            TokenScope scope,
            String message
    ) {}
}