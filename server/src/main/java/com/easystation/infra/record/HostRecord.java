package com.easystation.infra.record;

import com.easystation.infra.domain.enums.HostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HostRecord(
    UUID id,
    String identifier,
    String name,
    String hostname,
    String os,
    String cpuInfo,
    String memInfo,
    String ip,
    Integer port,
    HostStatus status,
    UUID environmentId,
    String environmentName,
    String description,
    List<String> tags,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastSeenAt,
    LocalDateTime lastHeartbeat,
    String config,
    Integer heartbeatInterval,
    String gatewayUrl,
    Integer listenPort
) {
    public record Create(
        String identifier,
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Hostname cannot be blank")
        String hostname,
        String os,
        String ip,
        Integer port,
        @NotNull(message = "Environment ID cannot be null")
        UUID environmentId,
        String description,
        List<String> tags,
        String gatewayUrl,
        Integer listenPort
    ) {}

    public record Update(
        String identifier,
        String name,
        String hostname,
        String os,
        String ip,
        Integer port,
        String description,
        UUID environmentId,
        List<String> tags,
        Boolean enabled,
        String config,
        Integer heartbeatInterval,
        String gatewayUrl,
        Integer listenPort
    ) {}
}
