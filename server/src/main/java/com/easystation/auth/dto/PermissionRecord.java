package com.easystation.auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PermissionRecord {

    public record Create(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            String resource,
            String action,
            Boolean system
    ) {}

    public record Update(
            String name,
            String description,
            String resource,
            String action
    ) {}

    public record Detail(
            UUID id,
            String code,
            String name,
            String description,
            String resource,
            String action,
            boolean system,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record Query(
            String keyword,
            String resource,
            String action,
            Integer limit,
            Integer offset
    ) {}

    public record CheckRequest(
            UUID userId,
            String permissionCode
    ) {}

    public record CheckResult(
            boolean allowed,
            String permissionCode,
            String message
    ) {}

    public record UserPermissions(
            UUID userId,
            String username,
            List<String> roles,
            List<String> permissions
    ) {}

    public record AssignPermissions(
            UUID roleId,
            List<UUID> permissionIds
    ) {}
}