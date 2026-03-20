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
                getAlertSettings()
        );
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