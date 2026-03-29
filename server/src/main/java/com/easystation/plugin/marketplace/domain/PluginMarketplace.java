package com.easystation.plugin.marketplace.domain;

import com.easystation.plugin.marketplace.dto.*;
import java.util.List;

/**
 * 插件市场服务接口
 */
public interface PluginMarketplace {
    
    /**
     * 获取插件列表
     */
    PluginListResponse listPlugins(int page, int pageSize);
    
    /**
     * 获取插件详情
     */
    PluginDetailResponse getPluginDetail(String pluginId);
    
    /**
     * 搜索插件
     */
    PluginListResponse searchPlugins(PluginSearchRequest request);
    
    /**
     * 安装插件
     */
    void installPlugin(PluginInstallRequest request);
    
    /**
     * 卸载插件
     */
    void uninstallPlugin(String pluginId);
    
    /**
     * 评分插件
     */
    void ratePlugin(String pluginId, int rating, String comment);
}
