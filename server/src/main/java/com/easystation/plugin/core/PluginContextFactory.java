package com.easystation.plugin.core;

/**
 * 插件上下文工厂
 * 
 * 负责创建插件上下文实例。
 */
public interface PluginContextFactory {
    
    /**
     * 创建插件上下文
     * @param descriptor 插件描述符
     * @param plugin 插件实例
     * @return 插件上下文
     */
    PluginContext createContext(PluginDescriptor descriptor, Plugin plugin);
}
