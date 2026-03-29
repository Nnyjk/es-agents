package com.easystation.plugin.core;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 插件加载器接口
 * 
 * 负责插件的加载、初始化、启动、停止和卸载。
 */
public interface PluginLoader {
    
    /**
     * 扫描插件目录
     * @param pluginDir 插件目录
     * @return 发现的插件描述符列表
     */
    List<PluginDescriptor> scan(Path pluginDir);
    
    /**
     * 加载插件
     * @param descriptor 插件描述符
     * @return 加载的插件实例
     * @throws PluginException 加载失败时抛出
     */
    Plugin load(PluginDescriptor descriptor) throws PluginException;
    
    /**
     * 初始化插件
     * @param plugin 插件实例
     * @param context 插件上下文
     * @throws PluginException 初始化失败时抛出
     */
    void initialize(Plugin plugin, PluginContext context) throws PluginException;
    
    /**
     * 启动插件
     * @param plugin 插件实例
     * @throws PluginException 启动失败时抛出
     */
    void start(Plugin plugin) throws PluginException;
    
    /**
     * 停止插件
     * @param plugin 插件实例
     */
    void stop(Plugin plugin);
    
    /**
     * 卸载插件
     * @param plugin 插件实例
     */
    void unload(Plugin plugin);
    
    /**
     * 获取插件实例
     * @param pluginId 插件 ID
     * @return 插件实例
     */
    Optional<Plugin> getPlugin(String pluginId);
    
    /**
     * 获取所有已加载的插件
     * @return 插件列表
     */
    List<Plugin> getAllPlugins();
    
    /**
     * 获取指定状态的插件
     * @param state 插件状态
     * @return 插件列表
     */
    List<Plugin> getPluginsByState(PluginState state);
}
