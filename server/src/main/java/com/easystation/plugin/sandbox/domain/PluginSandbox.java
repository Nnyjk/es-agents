package com.easystation.plugin.sandbox.domain;

import java.util.List;
import java.util.function.Supplier;

/**
 * 插件沙箱接口
 */
public interface PluginSandbox {
    
    /**
     * 创建安全上下文
     */
    SecurityContext createContext(String pluginId, List<Permission> permissions, ResourceLimit resourceLimit);
    
    /**
     * 获取安全上下文
     */
    SecurityContext getContext(String pluginId);
    
    /**
     * 移除安全上下文
     */
    void removeContext(String pluginId);
    
    /**
     * 检查权限
     */
    void checkPermission(SecurityContext context, PermissionType type, String target);
    
    /**
     * 执行受保护的操作
     */
    <T> T execute(SecurityContext context, Supplier<T> action);
    
    /**
     * 执行受保护的操作（无返回值）
     */
    void executeVoid(SecurityContext context, Runnable action);
}
