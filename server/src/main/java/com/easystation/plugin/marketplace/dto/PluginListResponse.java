package com.easystation.plugin.marketplace.dto;

import java.util.List;

/**
 * 插件列表响应
 */
public record PluginListResponse(
    List<PluginInfoDTO> plugins,
    int total,
    int page,
    int pageSize
) {
    public static PluginListResponse of(List<PluginInfoDTO> plugins, int total, int page, int pageSize) {
        return new PluginListResponse(plugins, total, page, pageSize);
    }
}
