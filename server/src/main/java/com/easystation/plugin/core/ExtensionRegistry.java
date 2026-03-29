package com.easystation.plugin.core;

import java.util.List;
import java.util.Optional;

/**
 * 扩展注册表
 * 
 * 管理扩展点的注册和发现。
 */
public interface ExtensionRegistry {
    
    /**
     * 注册扩展点
     * @param extensionPoint 扩展点实例
     * @param plugin 提供扩展点的插件
     */
    <T> void registerExtensionPoint(ExtensionPoint<T> extensionPoint, Plugin plugin);
    
    /**
     * 注销扩展点
     * @param name 扩展点名称
     * @param plugin 提供扩展点的插件
     */
    void unregisterExtensionPoint(String name, Plugin plugin);
    
    /**
     * 注册扩展实现
     * @param extensionPoint 扩展点名称
     * @param extension 扩展实现
     * @param plugin 提供扩展的插件
     */
    <T> void registerExtension(String extensionPoint, T extension, Plugin plugin);
    
    /**
     * 注销扩展实现
     * @param extensionPoint 扩展点名称
     * @param extension 扩展实现
     * @param plugin 提供扩展的插件
     */
    <T> void unregisterExtension(String extensionPoint, T extension, Plugin plugin);
    
    /**
     * 获取扩展点
     * @param name 扩展点名称
     * @return 扩展点
     */
    <T> Optional<ExtensionPoint<T>> getExtensionPoint(String name);
    
    /**
     * 获取所有扩展点
     * @return 扩展点列表
     */
    List<ExtensionPoint<?>> getAllExtensionPoints();
    
    /**
     * 获取扩展实现
     * @param extensionPoint 扩展点名称
     * @return 扩展实现列表
     */
    <T> List<T> getExtensions(String extensionPoint);
    
    /**
     * 获取指定插件提供的扩展
     * @param extensionPoint 扩展点名称
     * @param plugin 插件
     * @return 扩展实现列表
     */
    <T> List<T> getExtensionsByPlugin(String extensionPoint, Plugin plugin);
    
    /**
     * 获取指定插件提供的所有扩展
     * @param plugin 插件
     * @return 扩展实现列表
     */
    List<Object> getAllExtensionsByPlugin(Plugin plugin);
}
