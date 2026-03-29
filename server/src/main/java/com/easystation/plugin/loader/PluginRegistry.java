package com.easystation.plugin.loader;

import com.easystation.plugin.core.Plugin;
import com.easystation.plugin.core.PluginState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 插件注册表
 * 
 * 维护所有已加载插件的注册信息，提供插件查询和管理功能。
 * 线程安全实现，支持并发访问。
 */
public class PluginRegistry {
    
    /** 已加载插件的映射：pluginId -> Plugin */
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    
    /**
     * 注册插件
     * 
     * @param plugin 插件实例
     * @throws IllegalStateException 如果插件 ID 已存在
     */
    public void register(Plugin plugin) {
        String pluginId = plugin.getDescriptor().getId();
        if (plugins.containsKey(pluginId)) {
            throw new IllegalStateException("Plugin already registered: " + pluginId);
        }
        plugins.put(pluginId, plugin);
    }
    
    /**
     * 注销插件
     * 
     * @param pluginId 插件 ID
     * @return 被注销的插件，如果不存在则返回 null
     */
    public Plugin unregister(String pluginId) {
        return plugins.remove(pluginId);
    }
    
    /**
     * 获取插件实例
     * 
     * @param pluginId 插件 ID
     * @return 插件实例，如果不存在则返回 Optional.empty()
     */
    public Optional<Plugin> getPlugin(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }
    
    /**
     * 获取所有已加载的插件
     * 
     * @return 插件列表（不可变）
     */
    public List<Plugin> getAllPlugins() {
        return Collections.unmodifiableList(
            plugins.values().stream().collect(Collectors.toList())
        );
    }
    
    /**
     * 获取指定状态的插件列表
     * 
     * @param state 插件状态
     * @return 插件列表
     */
    public List<Plugin> getPluginsByState(PluginState state) {
        return plugins.values().stream()
            .filter(p -> p.getState() == state)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取已加载插件数量
     * 
     * @return 插件数量
     */
    public int size() {
        return plugins.size();
    }
    
    /**
     * 检查插件是否已注册
     * 
     * @param pluginId 插件 ID
     * @return 是否已注册
     */
    public boolean isRegistered(String pluginId) {
        return plugins.containsKey(pluginId);
    }
    
    /**
     * 清空注册表
     */
    public void clear() {
        plugins.clear();
    }
}
