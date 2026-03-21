package com.easystation.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AgentSourceVersionRecord {

    public record Create(
            @NotNull UUID sourceId,
            @NotBlank String version,
            String filePath,
            Long fileSize,
            String checksumMd5,
            String checksumSha256,
            String description,
            String downloadUrl,
            String createdBy
    ) {}

    public record Update(
            String version,
            String filePath,
            Long fileSize,
            String checksumMd5,
            String checksumSha256,
            String description,
            String downloadUrl
    ) {}

    public record Detail(
            UUID id,
            UUID sourceId,
            String version,
            String filePath,
            Long fileSize,
            String checksumMd5,
            String checksumSha256,
            String description,
            boolean verified,
            LocalDateTime verifiedAt,
            String verifiedBy,
            String downloadUrl,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record Query(
            UUID sourceId,
            String version,
            Boolean verified,
            Integer limit,
            Integer offset
    ) {}

    public record VerifyRequest(
            String verifiedBy,
            boolean verified
    ) {}

    public record CacheDetail(
            UUID id,
            UUID sourceId,
            UUID versionId,
            String cachePath,
            Long cacheSize,
            boolean valid,
            LocalDateTime lastAccessedAt,
            LocalDateTime expiresAt,
            LocalDateTime createdAt
    ) {}

    public record CacheQuery(
            UUID sourceId,
            Boolean valid,
            Integer limit,
            Integer offset
    ) {}

    public record PullRequest(
            UUID sourceId,
            String version,
            boolean useCache,
            String pulledBy
    ) {}

    public record PullResult(
            UUID sourceId,
            String version,
            String cachePath,
            Long fileSize,
            String checksumMd5,
            boolean fromCache
    ) {}
}