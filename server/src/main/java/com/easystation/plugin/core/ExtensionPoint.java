package com.easystation.plugin.core;

/**
 * 扩展点接口
 * 
 * 定义插件可扩展的扩展点，其他插件可以通过实现此接口来扩展功能。
 * 
 * @param <T> 扩展数据类型
 */
public interface ExtensionPoint<T> {
    
    /**
     * 获取扩展点名称
     * @return 扩展点名称
     */
    String getName();
    
    /**
     * 获取扩展点描述
     * @return 扩展点描述
     */
    String getDescription();
    
    /**
     * 获取扩展点类型
     * @return 扩展点类型
     */
    Class<T> getType();
    
    /**
     * 注册扩展
     * @param extension 扩展实现
     * @param plugin 提供扩展的插件
     */
    void register(T extension, Plugin plugin);
    
    /**
     * 注销扩展
     * @param extension 扩展实现
     */
    void unregister(T extension);
    
    /**
     * 获取所有注册的扩展
     * @return 扩展列表
     */
    java.util.List<T> getExtensions();
    
    /**
     * 获取指定插件提供的扩展
     * @param plugin 插件
     * @return 扩展列表
     */
    java.util.List<T> getExtensionsByPlugin(Plugin plugin);
}
