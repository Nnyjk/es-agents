package com.easystation.config.dto;

/**
 * 配置更新请求
 */
public class ConfigUpdateRequest {

    public String configValue;
    public String description;
    public String configType;
    public String changeReason;

    public ConfigUpdateRequest() {}

    public ConfigUpdateRequest(String configValue) {
        this.configValue = configValue;
    }
}
