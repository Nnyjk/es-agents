package com.easystation.setting.service;

import com.easystation.setting.domain.SystemSetting;
import com.easystation.setting.dto.SettingRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SystemSettingService {

    private static final String CATEGORY_BASIC = "basic";
    private static final String CATEGORY_SECURITY = "security";
    private static final String CATEGORY_ALERT = "alert";
    private static final String CATEGORY_STORAGE = "storage";
    private static final String CATEGORY_RESOURCE = "resource";
    private static final String CATEGORY_NOTIFICATION = "notification";
    private static final String CATEGORY_MAINTENANCE = "maintenance";
    private static final String CATEGORY_INTEGRATION = "integration";

    private static final Map<String, String> DEFAULT_SETTINGS = Map.of(
            "platformName", "Easy-Station",
            "passwordMinLength", "8",
            "sessionTimeoutMinutes", "30",
            "maxLoginAttempts", "5",
            "alertConvergenceSeconds", "60",
            "alertRetryCount", "3"
    );

    @Inject
    ObjectMapper objectMapper;

    public List<SettingRecord.Detail> list(String category) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (category != null && !category.isBlank()) {
            sql.append(" and category = :category");
            params.put("category", category);
        }

        return SystemSetting.<SystemSetting>find(sql.toString(), params)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public SettingRecord.Detail get(String key) {
        SystemSetting setting = SystemSetting.find("key", key).firstResult();
        if (setting == null) {
            throw new WebApplicationException("Setting not found: " + key, Response.Status.NOT_FOUND);
        }
        return toDetail(setting);
    }

    @Transactional
    public SettingRecord.Detail update(SettingRecord.Update dto) {
        SystemSetting setting = SystemSetting.find("key", dto.key()).firstResult();
        if (setting == null) {
            setting = new SystemSetting();
            setting.key = dto.key();
            setting.category = determineCategory(dto.key());
            setting.description = getSettingDescription(dto.key());
        }
        setting.value = dto.value();
        setting.persist();
        Log.infof("Setting updated: %s = %s", dto.key(), dto.value());
        return toDetail(setting);
    }

    @Transactional
    public void batchUpdate(SettingRecord.BatchUpdate dto) {
        dto.settings().forEach((key, value) -> {
            SystemSetting setting = SystemSetting.find("key", key).firstResult();
            if (setting == null) {
                setting = new SystemSetting();
                setting.key = key;
                setting.category = determineCategory(key);
                setting.description = getSettingDescription(key);
            }
            setting.value = value;
            setting.persist();
        });
        Log.infof("Batch updated %d settings", dto.settings().size());
    }

    public SettingRecord.BasicSettings getBasicSettings() {
        return new SettingRecord.BasicSettings(
                getValue("platformName", "Easy-Station"),
                getValue("logoUrl"),
                getValue("faviconUrl"),
                getValue("footerText"),
                getValue("loginPageTitle"),
                getValue("loginPageBackground"),
                getValue("loginPageNotice")
        );
    }

    @Transactional
    public SettingRecord.BasicSettings updateBasicSettings(SettingRecord.BasicSettings dto) {
        updateIfPresent("platformName", dto.platformName());
        updateIfPresent("logoUrl", dto.logoUrl());
        updateIfPresent("faviconUrl", dto.faviconUrl());
        updateIfPresent("footerText", dto.footerText());
        updateIfPresent("loginPageTitle", dto.loginPageTitle());
        updateIfPresent("loginPageBackground", dto.loginPageBackground());
        updateIfPresent("loginPageNotice", dto.loginPageNotice());
        return getBasicSettings();
    }

    public SettingRecord.SecuritySettings getSecuritySettings() {
        return new SettingRecord.SecuritySettings(
                getIntValue("passwordMinLength", 8),
                getBoolValue("passwordRequireUppercase", true),
                getBoolValue("passwordRequireLowercase", true),
                getBoolValue("passwordRequireNumber", true),
                getBoolValue("passwordRequireSpecial", false),
                getIntValue("sessionTimeoutMinutes", 30),
                getIntValue("maxLoginAttempts", 5),
                getValue("ipWhitelist")
        );
    }

    @Transactional
    public SettingRecord.SecuritySettings updateSecuritySettings(SettingRecord.SecuritySettings dto) {
        updateIfPresent("passwordMinLength", dto.passwordMinLength() != null ? String.valueOf(dto.passwordMinLength()) : null);
        updateIfPresent("passwordRequireUppercase", dto.passwordRequireUppercase() != null ? String.valueOf(dto.passwordRequireUppercase()) : null);
        updateIfPresent("passwordRequireLowercase", dto.passwordRequireLowercase() != null ? String.valueOf(dto.passwordRequireLowercase()) : null);
        updateIfPresent("passwordRequireNumber", dto.passwordRequireNumber() != null ? String.valueOf(dto.passwordRequireNumber()) : null);
        updateIfPresent("passwordRequireSpecial", dto.passwordRequireSpecial() != null ? String.valueOf(dto.passwordRequireSpecial()) : null);
        updateIfPresent("sessionTimeoutMinutes", dto.sessionTimeoutMinutes() != null ? String.valueOf(dto.sessionTimeoutMinutes()) : null);
        updateIfPresent("maxLoginAttempts", dto.maxLoginAttempts() != null ? String.valueOf(dto.maxLoginAttempts()) : null);
        updateIfPresent("ipWhitelist", dto.ipWhitelist());
        return getSecuritySettings();
    }

    public SettingRecord.AlertSettings getAlertSettings() {
        return new SettingRecord.AlertSettings(
                getUuidValue("defaultAlertChannelId"),
                getBoolValue("alertEnabled", true),
                getIntValue("alertConvergenceSeconds", 60),
                getIntValue("alertRetryCount", 3)
        );
    }

    @Transactional
    public SettingRecord.AlertSettings updateAlertSettings(SettingRecord.AlertSettings dto) {
        updateIfPresent("defaultAlertChannelId", dto.defaultAlertChannelId() != null ? String.valueOf(dto.defaultAlertChannelId()) : null);
        updateIfPresent("alertEnabled", dto.alertEnabled() != null ? String.valueOf(dto.alertEnabled()) : null);
        updateIfPresent("alertConvergenceSeconds", dto.alertConvergenceSeconds() != null ? String.valueOf(dto.alertConvergenceSeconds()) : null);
        updateIfPresent("alertRetryCount", dto.alertRetryCount() != null ? String.valueOf(dto.alertRetryCount()) : null);
        return getAlertSettings();
    }

    public SettingRecord.AllSettings getAllSettings() {
        return new SettingRecord.AllSettings(
                getBasicSettings(),
                getSecuritySettings(),
                getAlertSettings(),
                getStorageSettings(),
                getResourceSettings(),
                getNotificationSettings(),
                getMaintenanceSettings(),
                getIntegrationSettings()
        );
    }

    // ==================== 存储配置 ====================

    public SettingRecord.StorageSettings getStorageSettings() {
        return new SettingRecord.StorageSettings(
                getValue("storageType", "local"),
                getValue("localStoragePath", "/data/files"),
                getValue("ossEndpoint"),
                getValue("ossBucket"),
                getValue("ossAccessKey"),
                getValue("ossSecretKey"),
                getValue("ossRegion"),
                getIntValue("maxFileSizeMb", 100),
                getValue("allowedFileTypes")
        );
    }

    @Transactional
    public SettingRecord.StorageSettings updateStorageSettings(SettingRecord.StorageSettings dto) {
        updateIfPresent("storageType", dto.storageType());
        updateIfPresent("localStoragePath", dto.localStoragePath());
        updateIfPresent("ossEndpoint", dto.ossEndpoint());
        updateIfPresent("ossBucket", dto.ossBucket());
        updateIfPresent("ossAccessKey", dto.ossAccessKey());
        updateIfPresent("ossSecretKey", dto.ossSecretKey());
        updateIfPresent("ossRegion", dto.ossRegion());
        updateIfPresent("maxFileSizeMb", dto.maxFileSizeMb() != null ? String.valueOf(dto.maxFileSizeMb()) : null);
        updateIfPresent("allowedFileTypes", dto.allowedFileTypes());
        return getStorageSettings();
    }

    // ==================== 资源配置 ====================

    public SettingRecord.ResourceSettings getResourceSettings() {
        return new SettingRecord.ResourceSettings(
                getIntValue("defaultResourceQuota", 100),
                getIntValue("maxTenantQuota", 1000),
                getBoolValue("overQuotaAllowed", false),
                getIntValue("logRetentionDays", 30),
                getValue("logStoragePath", "/data/logs"),
                getBoolValue("logArchiveEnabled", true),
                getIntValue("cacheExpireMinutes", 60),
                getValue("cacheCleanupPolicy", "LRU")
        );
    }

    @Transactional
    public SettingRecord.ResourceSettings updateResourceSettings(SettingRecord.ResourceSettings dto) {
        updateIfPresent("defaultResourceQuota", dto.defaultResourceQuota() != null ? String.valueOf(dto.defaultResourceQuota()) : null);
        updateIfPresent("maxTenantQuota", dto.maxTenantQuota() != null ? String.valueOf(dto.maxTenantQuota()) : null);
        updateIfPresent("overQuotaAllowed", dto.overQuotaAllowed() != null ? String.valueOf(dto.overQuotaAllowed()) : null);
        updateIfPresent("logRetentionDays", dto.logRetentionDays() != null ? String.valueOf(dto.logRetentionDays()) : null);
        updateIfPresent("logStoragePath", dto.logStoragePath());
        updateIfPresent("logArchiveEnabled", dto.logArchiveEnabled() != null ? String.valueOf(dto.logArchiveEnabled()) : null);
        updateIfPresent("cacheExpireMinutes", dto.cacheExpireMinutes() != null ? String.valueOf(dto.cacheExpireMinutes()) : null);
        updateIfPresent("cacheCleanupPolicy", dto.cacheCleanupPolicy());
        return getResourceSettings();
    }

    // ==================== 通知配置 ====================

    public SettingRecord.NotificationSettings getNotificationSettings() {
        return new SettingRecord.NotificationSettings(
                getValue("emailSmtpHost"),
                getIntValue("emailSmtpPort", 25),
                getValue("emailUsername"),
                getValue("emailPassword"),
                getBoolValue("emailEnabled", false),
                getValue("smsProvider"),
                getValue("smsApiKey"),
                getBoolValue("smsEnabled", false),
                getValue("wechatWebhook"),
                getBoolValue("wechatEnabled", false),
                getValue("dingtalkWebhook"),
                getBoolValue("dingtalkEnabled", false),
                getValue("feishuWebhook"),
                getBoolValue("feishuEnabled", false)
        );
    }

    @Transactional
    public SettingRecord.NotificationSettings updateNotificationSettings(SettingRecord.NotificationSettings dto) {
        updateIfPresent("emailSmtpHost", dto.emailSmtpHost());
        updateIfPresent("emailSmtpPort", dto.emailSmtpPort() != null ? String.valueOf(dto.emailSmtpPort()) : null);
        updateIfPresent("emailUsername", dto.emailUsername());
        updateIfPresent("emailPassword", dto.emailPassword());
        updateIfPresent("emailEnabled", dto.emailEnabled() != null ? String.valueOf(dto.emailEnabled()) : null);
        updateIfPresent("smsProvider", dto.smsProvider());
        updateIfPresent("smsApiKey", dto.smsApiKey());
        updateIfPresent("smsEnabled", dto.smsEnabled() != null ? String.valueOf(dto.smsEnabled()) : null);
        updateIfPresent("wechatWebhook", dto.wechatWebhook());
        updateIfPresent("wechatEnabled", dto.wechatEnabled() != null ? String.valueOf(dto.wechatEnabled()) : null);
        updateIfPresent("dingtalkWebhook", dto.dingtalkWebhook());
        updateIfPresent("dingtalkEnabled", dto.dingtalkEnabled() != null ? String.valueOf(dto.dingtalkEnabled()) : null);
        updateIfPresent("feishuWebhook", dto.feishuWebhook());
        updateIfPresent("feishuEnabled", dto.feishuEnabled() != null ? String.valueOf(dto.feishuEnabled()) : null);
        return getNotificationSettings();
    }

    // ==================== 维护配置 ====================

    public SettingRecord.MaintenanceSettings getMaintenanceSettings() {
        return new SettingRecord.MaintenanceSettings(
                getBoolValue("maintenanceMode", false),
                getValue("maintenanceMessage", "系统维护中，请稍后访问"),
                getValue("maintenanceWhitelist"),
                getValue("licenseKey"),
                getBoolValue("licenseValid", false),
                getValue("licenseExpiry")
        );
    }

    @Transactional
    public SettingRecord.MaintenanceSettings updateMaintenanceSettings(SettingRecord.MaintenanceSettings dto) {
        updateIfPresent("maintenanceMode", dto.maintenanceMode() != null ? String.valueOf(dto.maintenanceMode()) : null);
        updateIfPresent("maintenanceMessage", dto.maintenanceMessage());
        updateIfPresent("maintenanceWhitelist", dto.maintenanceWhitelist());
        updateIfPresent("licenseKey", dto.licenseKey());
        // licenseValid 和 licenseExpiry 由系统自动更新，不直接修改
        return getMaintenanceSettings();
    }

    // ==================== 集成配置 ====================

    public SettingRecord.IntegrationSettings getIntegrationSettings() {
        return new SettingRecord.IntegrationSettings(
                getBoolValue("oauthEnabled", false),
                getValue("oauthProvider"),
                getValue("oauthClientId"),
                getValue("oauthClientSecret"),
                getBoolValue("ldapEnabled", false),
                getValue("ldapUrl"),
                getValue("ldapBaseDn"),
                getValue("ldapBindDn"),
                getValue("ldapBindPassword"),
                getBoolValue("ssoEnabled", false),
                getValue("ssoEntryPoint"),
                getValue("ssoCertificate")
        );
    }

    @Transactional
    public SettingRecord.IntegrationSettings updateIntegrationSettings(SettingRecord.IntegrationSettings dto) {
        updateIfPresent("oauthEnabled", dto.oauthEnabled() != null ? String.valueOf(dto.oauthEnabled()) : null);
        updateIfPresent("oauthProvider", dto.oauthProvider());
        updateIfPresent("oauthClientId", dto.oauthClientId());
        updateIfPresent("oauthClientSecret", dto.oauthClientSecret());
        updateIfPresent("ldapEnabled", dto.ldapEnabled() != null ? String.valueOf(dto.ldapEnabled()) : null);
        updateIfPresent("ldapUrl", dto.ldapUrl());
        updateIfPresent("ldapBaseDn", dto.ldapBaseDn());
        updateIfPresent("ldapBindDn", dto.ldapBindDn());
        updateIfPresent("ldapBindPassword", dto.ldapBindPassword());
        updateIfPresent("ssoEnabled", dto.ssoEnabled() != null ? String.valueOf(dto.ssoEnabled()) : null);
        updateIfPresent("ssoEntryPoint", dto.ssoEntryPoint());
        updateIfPresent("ssoCertificate", dto.ssoCertificate());
        return getIntegrationSettings();
    }

    @Transactional
    public void initializeDefaultSettings() {
        DEFAULT_SETTINGS.forEach((key, value) -> {
            if (SystemSetting.find("key", key).firstResult() == null) {
                SystemSetting setting = new SystemSetting();
                setting.key = key;
                setting.value = value;
                setting.category = determineCategory(key);
                setting.description = getSettingDescription(key);
                setting.persist();
            }
        });
        Log.info("Default settings initialized");
    }

    private String getValue(String key, String defaultValue) {
        SystemSetting setting = SystemSetting.find("key", key).firstResult();
        return setting != null && setting.value != null ? setting.value : defaultValue;
    }

    private String getValue(String key) {
        return getValue(key, null);
    }

    private Integer getIntValue(String key, Integer defaultValue) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Boolean getBoolValue(String key, Boolean defaultValue) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    private UUID getUuidValue(String key) {
        String value = getValue(key);
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void updateIfPresent(String key, String value) {
        if (value == null) return;
        SystemSetting setting = SystemSetting.find("key", key).firstResult();
        if (setting == null) {
            setting = new SystemSetting();
            setting.key = key;
            setting.category = determineCategory(key);
            setting.description = getSettingDescription(key);
        }
        setting.value = value;
        setting.persist();
    }

    private String determineCategory(String key) {
        if (Set.of("platformName", "logoUrl", "faviconUrl", "footerText",
                   "loginPageTitle", "loginPageBackground", "loginPageNotice").contains(key)) {
            return CATEGORY_BASIC;
        }
        if (Set.of("passwordMinLength", "passwordRequireUppercase", "passwordRequireLowercase",
                   "passwordRequireNumber", "passwordRequireSpecial", "sessionTimeoutMinutes",
                   "maxLoginAttempts", "ipWhitelist").contains(key)) {
            return CATEGORY_SECURITY;
        }
        if (Set.of("defaultAlertChannelId", "alertEnabled", "alertConvergenceSeconds",
                   "alertRetryCount").contains(key)) {
            return CATEGORY_ALERT;
        }
        if (Set.of("storageType", "localStoragePath", "ossEndpoint", "ossBucket",
                   "ossAccessKey", "ossSecretKey", "ossRegion", "maxFileSizeMb",
                   "allowedFileTypes").contains(key)) {
            return CATEGORY_STORAGE;
        }
        if (Set.of("defaultResourceQuota", "maxTenantQuota", "overQuotaAllowed",
                   "logRetentionDays", "logStoragePath", "logArchiveEnabled",
                   "cacheExpireMinutes", "cacheCleanupPolicy").contains(key)) {
            return CATEGORY_RESOURCE;
        }
        if (Set.of("emailSmtpHost", "emailSmtpPort", "emailUsername", "emailPassword",
                   "emailEnabled", "smsProvider", "smsApiKey", "smsEnabled",
                   "wechatWebhook", "wechatEnabled", "dingtalkWebhook", "dingtalkEnabled",
                   "feishuWebhook", "feishuEnabled").contains(key)) {
            return CATEGORY_NOTIFICATION;
        }
        if (Set.of("maintenanceMode", "maintenanceMessage", "maintenanceWhitelist",
                   "licenseKey", "licenseValid", "licenseExpiry").contains(key)) {
            return CATEGORY_MAINTENANCE;
        }
        if (Set.of("oauthEnabled", "oauthProvider", "oauthClientId", "oauthClientSecret",
                   "ldapEnabled", "ldapUrl", "ldapBaseDn", "ldapBindDn", "ldapBindPassword",
                   "ssoEnabled", "ssoEntryPoint", "ssoCertificate").contains(key)) {
            return CATEGORY_INTEGRATION;
        }
        return "other";
    }

    private String getSettingDescription(String key) {
        return switch (key) {
            case "platformName" -> "平台名称";
            case "logoUrl" -> "Logo 图片地址";
            case "faviconUrl" -> "网站图标地址";
            case "footerText" -> "页脚文字";
            case "loginPageTitle" -> "登录页标题";
            case "loginPageBackground" -> "登录页背景图";
            case "loginPageNotice" -> "登录页公告";
            case "passwordMinLength" -> "密码最小长度";
            case "passwordRequireUppercase" -> "密码要求大写字母";
            case "passwordRequireLowercase" -> "密码要求小写字母";
            case "passwordRequireNumber" -> "密码要求数字";
            case "passwordRequireSpecial" -> "密码要求特殊字符";
            case "sessionTimeoutMinutes" -> "会话超时时间(分钟)";
            case "maxLoginAttempts" -> "最大登录尝试次数";
            case "ipWhitelist" -> "IP 白名单";
            case "defaultAlertChannelId" -> "默认告警渠道 ID";
            case "alertEnabled" -> "告警启用开关";
            case "alertConvergenceSeconds" -> "告警收敛时间(秒)";
            case "alertRetryCount" -> "告警重试次数";
            // 存储配置
            case "storageType" -> "存储类型";
            case "localStoragePath" -> "本地存储路径";
            case "ossEndpoint" -> "对象存储 Endpoint";
            case "ossBucket" -> "对象存储桶名";
            case "ossAccessKey" -> "对象存储 AccessKey";
            case "ossSecretKey" -> "对象存储 SecretKey";
            case "ossRegion" -> "对象存储区域";
            case "maxFileSizeMb" -> "最大文件大小(MB)";
            case "allowedFileTypes" -> "允许的文件类型";
            // 资源配置
            case "defaultResourceQuota" -> "默认资源配额";
            case "maxTenantQuota" -> "单租户最大配额";
            case "overQuotaAllowed" -> "允许超配";
            case "logRetentionDays" -> "日志保留天数";
            case "logStoragePath" -> "日志存储路径";
            case "logArchiveEnabled" -> "启用日志归档";
            case "cacheExpireMinutes" -> "缓存过期时间(分钟)";
            case "cacheCleanupPolicy" -> "缓存清理策略";
            // 通知配置
            case "emailSmtpHost" -> "SMTP 服务器地址";
            case "emailSmtpPort" -> "SMTP 端口";
            case "emailUsername" -> "邮箱用户名";
            case "emailPassword" -> "邮箱密码";
            case "emailEnabled" -> "启用邮件通知";
            case "smsProvider" -> "短信服务商";
            case "smsApiKey" -> "短信 API Key";
            case "smsEnabled" -> "启用短信通知";
            case "wechatWebhook" -> "企业微信 Webhook";
            case "wechatEnabled" -> "启用企业微信通知";
            case "dingtalkWebhook" -> "钉钉 Webhook";
            case "dingtalkEnabled" -> "启用钉钉通知";
            case "feishuWebhook" -> "飞书 Webhook";
            case "feishuEnabled" -> "启用飞书通知";
            // 维护配置
            case "maintenanceMode" -> "维护模式";
            case "maintenanceMessage" -> "维护提示语";
            case "maintenanceWhitelist" -> "维护模式白名单 IP";
            case "licenseKey" -> "License Key";
            case "licenseValid" -> "License 有效";
            case "licenseExpiry" -> "License 过期时间";
            // 集成配置
            case "oauthEnabled" -> "启用 OAuth";
            case "oauthProvider" -> "OAuth 提供商";
            case "oauthClientId" -> "OAuth Client ID";
            case "oauthClientSecret" -> "OAuth Client Secret";
            case "ldapEnabled" -> "启用 LDAP";
            case "ldapUrl" -> "LDAP 服务器地址";
            case "ldapBaseDn" -> "LDAP Base DN";
            case "ldapBindDn" -> "LDAP Bind DN";
            case "ldapBindPassword" -> "LDAP 绑定密码";
            case "ssoEnabled" -> "启用 SSO";
            case "ssoEntryPoint" -> "SSO 入口地址";
            case "ssoCertificate" -> "SSO 证书";
            default -> "";
        };
    }

    private SettingRecord.Detail toDetail(SystemSetting setting) {
        return new SettingRecord.Detail(
                setting.id,
                setting.key,
                setting.value,
                setting.category,
                setting.description,
                setting.createdAt,
                setting.updatedAt
        );
    }
}