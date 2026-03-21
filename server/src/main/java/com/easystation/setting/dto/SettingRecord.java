package com.easystation.setting.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class SettingRecord {

    public record Update(
            @NotBlank String key,
            String value
    ) {}

    public record BatchUpdate(
            Map<String, String> settings
    ) {}

    public record Detail(
            UUID id,
            String key,
            String value,
            String category,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record BasicSettings(
            String platformName,
            String logoUrl,
            String faviconUrl,
            String footerText,
            String loginPageTitle,
            String loginPageBackground,
            String loginPageNotice
    ) {}

    public record SecuritySettings(
            Integer passwordMinLength,
            Boolean passwordRequireUppercase,
            Boolean passwordRequireLowercase,
            Boolean passwordRequireNumber,
            Boolean passwordRequireSpecial,
            Integer sessionTimeoutMinutes,
            Integer maxLoginAttempts,
            String ipWhitelist
    ) {}

    public record AlertSettings(
            UUID defaultAlertChannelId,
            Boolean alertEnabled,
            Integer alertConvergenceSeconds,
            Integer alertRetryCount
    ) {}

    public record AllSettings(
            BasicSettings basic,
            SecuritySettings security,
            AlertSettings alert
    ) {}
}