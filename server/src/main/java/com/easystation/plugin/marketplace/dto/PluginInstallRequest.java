package com.easystation.plugin.marketplace.dto;

/**
 * 插件安装请求
 */
public record PluginInstallRequest(
    String pluginId,
    String version
) {
    public PluginInstallRequest {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("pluginId is required");
        }
    }
}
