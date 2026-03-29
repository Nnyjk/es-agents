package com.easystation.plugin.core;

import java.util.Optional;

/**
 * 插件上下文
 * 
 * 提供给插件访问宿主环境的接口，包括服务、配置、日志等。
 */
public interface PluginContext {
    
    /**
     * 获取插件描述符
     * @return 插件描述符
     */
    PluginDescriptor getDescriptor();
    
    /**
     * 获取插件数据目录
     * @return 数据目录路径
     */
    String getDataDirectory();
    
    /**
     * 获取插件配置目录
     * @return 配置目录路径
     */
    String getConfigDirectory();
    
    /**
     * 获取插件日志目录
     * @return 日志目录路径
     */
    String getLogDirectory();
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    Optional<String> getConfig(String key);
    
    /**
     * 获取配置值（带默认值）
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    String getConfig(String key, String defaultValue);
    
    /**
     * 获取服务实例
     * @param serviceClass 服务类
     * @param <T> 服务类型
     * @return 服务实例
     */
    <T> T getService(Class<T> serviceClass);
    
    /**
     * 注册服务
     * @param serviceClass 服务类
     * @param service 服务实例
     * @param <T> 服务类型
     */
    <T> void registerService(Class<T> serviceClass, T service);
    
    /**
     * 获取扩展点实现
     * @param extensionPoint 扩展点名称
     * @param <T> 扩展点类型
     * @return 扩展点实现列表
     */
    <T> java.util.List<T> getExtensions(String extensionPoint);
    
    /**
     * 注册扩展点实现
     * @param extensionPoint 扩展点名称
     * @param extension 扩展点实现
     */
    void registerExtension(String extensionPoint, Object extension);
    
    /**
     * 发布事件
     * @param event 事件对象
     */
    void publishEvent(Object event);
    
    /**
     * 获取日志记录器
     * @param name 日志器名称
     * @return 日志记录器
     */
    org.slf4j.Logger getLogger(String name);
    
    /**
     * 获取插件类加载器
     * @return 类加载器
     */
    ClassLoader getClassLoader();
    
    /**
     * 检查插件是否已激活
     * @return 是否激活
     */
    boolean isActive();
}
