package com.easystation.infra.record;

import com.easystation.infra.domain.enums.HostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record HostRecord(
    UUID id,
    String name,
    String hostname,
    String os,
    String cpuInfo,
    String memInfo,
    HostStatus status,
    UUID environmentId,
    String environmentName,
    String description,
    LocalDateTime createdAt,
    LocalDateTime lastHeartbeat,
    String config,
    Integer heartbeatInterval,
    String gatewayUrl,
    Integer listenPort
) {
    public record Create(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Hostname cannot be blank")
        String hostname,
        @NotNull(message = "Environment ID cannot be null")
        UUID environmentId,
        String description,
        String gatewayUrl,
        Integer listenPort
    ) {}

    public record Update(
        String name,
        String hostname,
        String description,
        UUID environmentId,
        String config,
        Integer heartbeatInterval,
        String gatewayUrl,
        Integer listenPort
    ) {}
}
