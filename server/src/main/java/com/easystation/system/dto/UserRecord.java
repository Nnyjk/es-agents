package com.easystation.system.record;

import com.easystation.system.domain.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;

public record UserRecord(
    UUID id,
    String username,
    UserStatus status,
    Set<RoleRecord> roles
) {
    public record Create(
        @NotBlank
        String username,
        @NotBlank
        String password,
        Set<UUID> roleIds
    ) {}

    public record Update(
        UserStatus status,
        Set<UUID> roleIds
    ) {}

    public record Login(
        @NotBlank
        String username,
        @NotBlank
        String password
    ) {}
}
