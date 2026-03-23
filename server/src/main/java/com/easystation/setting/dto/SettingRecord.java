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

    /**
     * 存储配置
     */
    public record StorageSettings(
            String storageType,
            String localStoragePath,
            String ossEndpoint,
            String ossBucket,
            String ossAccessKey,
            String ossSecretKey,
            String ossRegion,
            Integer maxFileSizeMb,
            String allowedFileTypes
    ) {}

    /**
     * 资源配置
     */
    public record ResourceSettings(
            Integer defaultResourceQuota,
            Integer maxTenantQuota,
            Boolean overQuotaAllowed,
            Integer logRetentionDays,
            String logStoragePath,
            Boolean logArchiveEnabled,
            Integer cacheExpireMinutes,
            String cacheCleanupPolicy
    ) {}

    /**
     * 通知配置
     */
    public record NotificationSettings(
            String emailSmtpHost,
            Integer emailSmtpPort,
            String emailUsername,
            String emailPassword,
            Boolean emailEnabled,
            String smsProvider,
            String smsApiKey,
            Boolean smsEnabled,
            String wechatWebhook,
            Boolean wechatEnabled,
            String dingtalkWebhook,
            Boolean dingtalkEnabled,
            String feishuWebhook,
            Boolean feishuEnabled
    ) {}

    /**
     * 维护配置
     */
    public record MaintenanceSettings(
            Boolean maintenanceMode,
            String maintenanceMessage,
            String maintenanceWhitelist,
            String licenseKey,
            Boolean licenseValid,
            String licenseExpiry
    ) {}

    /**
     * 集成配置
     */
    public record IntegrationSettings(
            Boolean oauthEnabled,
            String oauthProvider,
            String oauthClientId,
            String oauthClientSecret,
            Boolean ldapEnabled,
            String ldapUrl,
            String ldapBaseDn,
            String ldapBindDn,
            String ldapBindPassword,
            Boolean ssoEnabled,
            String ssoEntryPoint,
            String ssoCertificate
    ) {}

    public record AllSettings(
            BasicSettings basic,
            SecuritySettings security,
            AlertSettings alert,
            StorageSettings storage,
            ResourceSettings resource,
            NotificationSettings notification,
            MaintenanceSettings maintenance,
            IntegrationSettings integration
    ) {}
}