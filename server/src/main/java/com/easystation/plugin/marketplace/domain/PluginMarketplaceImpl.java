package com.easystation.plugin.marketplace.domain;

import com.easystation.plugin.marketplace.dto.*;
import com.easystation.plugin.loader.PluginManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件市场服务实现
 */
@ApplicationScoped
public class PluginMarketplaceImpl implements PluginMarketplace {
    
    private static final Logger LOG = Logger.getLogger(PluginMarketplaceImpl.class);
    
    @Inject
    PluginManager pluginManager;
    
    // 模拟插件注册表（实际应从数据库或远程注册中心获取）
    private final Map<String, PluginInfoDTO> pluginRegistry = new ConcurrentHashMap<>();
    
    public PluginMarketplaceImpl() {
        // 初始化示例插件数据
        initializeSamplePlugins();
    }
    
    private void initializeSamplePlugins() {
        pluginRegistry.put("code-formatter", PluginInfoDTO.of(
            "code-formatter", "Code Formatter", "1.0.0",
            "代码格式化工具，支持 Java/Python/JavaScript",
            "ESA Team", "development", List.of("formatter", "code", "productivity"),
            1250, 4.5, 89, "available"
        ));
        
        pluginRegistry.put("git-integration", PluginInfoDTO.of(
            "git-integration", "Git Integration", "2.1.0",
            "Git 版本控制集成",
            "ESA Team", "integration", List.of("git", "vcs", "integration"),
            2340, 4.8, 156, "available"
        ));
        
        pluginRegistry.put("ai-assistant", PluginInfoDTO.of(
            "ai-assistant", "AI Assistant", "1.5.2",
            "AI 编程助手，支持代码补全和审查",
            "ESA Team", "ai", List.of("ai", "assistant", "coding"),
            5670, 4.9, 423, "available"
        ));
    }
    
    @Override
    public PluginListResponse listPlugins(int page, int pageSize) {
        LOG.infof("Listing plugins: page=%d, pageSize=%d", page, pageSize);
        
        List<PluginInfoDTO> allPlugins = new ArrayList<>(pluginRegistry.values());
        int total = allPlugins.size();
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<PluginInfoDTO> pagedPlugins = allPlugins.subList(fromIndex, toIndex);
        
        return PluginListResponse.of(pagedPlugins, total, page, pageSize);
    }
    
    @Override
    public PluginDetailResponse getPluginDetail(String pluginId) {
        LOG.infof("Getting plugin detail: %s", pluginId);
        
        PluginInfoDTO pluginInfo = pluginRegistry.get(pluginId);
        if (pluginInfo == null) {
            throw new PluginNotFoundException(pluginId);
        }
        
        return PluginDetailResponse.of(
            pluginInfo.id(), pluginInfo.name(), pluginInfo.version(),
            pluginInfo.description(), pluginInfo.author(), pluginInfo.category(),
            pluginInfo.tags(), pluginInfo.downloadCount(), pluginInfo.rating(),
            pluginInfo.reviewCount(), pluginInfo.status()
        );
    }
    
    @Override
    public PluginListResponse searchPlugins(PluginSearchRequest request) {
        LOG.infof("Searching plugins: keyword=%s, category=%s", 
            request.keyword(), request.category());
        
        List<PluginInfoDTO> allPlugins = new ArrayList<>(pluginRegistry.values());
        
        // 过滤
        List<PluginInfoDTO> filtered = allPlugins.stream()
            .filter(plugin -> {
                if (request.keyword() != null && !request.keyword().isBlank()) {
                    String keyword = request.keyword().toLowerCase();
                    boolean matchName = plugin.name().toLowerCase().contains(keyword);
                    boolean matchDesc = plugin.description().toLowerCase().contains(keyword);
                    boolean matchTags = plugin.tags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(keyword));
                    if (!matchName && !matchDesc && !matchTags) {
                        return false;
                    }
                }
                if (request.category() != null && !request.category().isBlank()) {
                    if (!request.category().equals(plugin.category())) {
                        return false;
                    }
                }
                return true;
            })
            .toList();
        
        int total = filtered.size();
        int fromIndex = (request.getPage() - 1) * request.getPageSize();
        int toIndex = Math.min(fromIndex + request.getPageSize(), total);
        
        List<PluginInfoDTO> pagedPlugins = filtered.subList(fromIndex, toIndex);
        
        return PluginListResponse.of(pagedPlugins, total, request.getPage(), request.getPageSize());
    }
    
    @Override
    public void installPlugin(PluginInstallRequest request) {
        LOG.infof("Installing plugin: %s (version=%s)", request.pluginId(), request.version());
        
        if (!pluginRegistry.containsKey(request.pluginId())) {
            throw new PluginNotFoundException(request.pluginId());
        }
        
        // 调用 PluginManager 安装插件
        // TODO: 实际实现需要从注册中心下载并安装
        LOG.infof("Plugin %s installation initiated", request.pluginId());
    }
    
    @Override
    public void uninstallPlugin(String pluginId) {
        LOG.infof("Uninstalling plugin: %s", pluginId);
        
        // 调用 PluginManager 卸载插件
        pluginManager.unloadPlugin(pluginId);
    }
    
    @Override
    public void ratePlugin(String pluginId, int rating, String comment) {
        LOG.infof("Rating plugin %s: %d stars", pluginId, rating);
        
        if (!pluginRegistry.containsKey(pluginId)) {
            throw new PluginNotFoundException(pluginId);
        }
        
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        // TODO: 实际实现需要保存评分和评论
    }
}
