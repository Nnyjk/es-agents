package com.easystation.plugin.core;

/**
 * 插件依赖
 * 
 * 描述插件对其他插件的依赖关系，包括版本要求。
 */
public class PluginDependency {
    
    private final String pluginId;
    private final String version;
    private final boolean optional;
    
    /**
     * 创建插件依赖
     * 
     * @param pluginId 依赖的插件 ID
     * @param version 版本要求（支持语义化版本表达式，如 ">=1.0.0"）
     * @param optional 是否为可选依赖
     */
    public PluginDependency(String pluginId, String version, boolean optional) {
        this.pluginId = pluginId;
        this.version = version;
        this.optional = optional;
    }
    
    /**
     * 创建必需的插件依赖
     * 
     * @param pluginId 依赖的插件 ID
     * @param version 版本要求
     */
    public PluginDependency(String pluginId, String version) {
        this(pluginId, version, false);
    }
    
    /**
     * 创建不指定版本的依赖
     * 
     * @param pluginId 依赖的插件 ID
     */
    public PluginDependency(String pluginId) {
        this(pluginId, "*", false);
    }
    
    /**
     * 获取依赖的插件 ID
     * 
     * @return 插件 ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * 获取版本要求
     * 
     * @return 版本表达式
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 是否为可选依赖
     * 
     * @return 是否可选
     */
    public boolean isOptional() {
        return optional;
    }
    
    /**
     * 检查版本是否匹配
     * 
     * @param actualVersion 实际版本
     * @return 是否匹配
     */
    public boolean isVersionCompatible(String actualVersion) {
        if ("*".equals(version)) {
            return true;
        }
        
        // 简单的版本匹配逻辑
        // TODO: 实现完整的语义化版本比较
        if (version.startsWith(">=")) {
            return compareVersions(actualVersion, version.substring(2)) >= 0;
        } else if (version.startsWith(">")) {
            return compareVersions(actualVersion, version.substring(1)) > 0;
        } else if (version.startsWith("<=")) {
            return compareVersions(actualVersion, version.substring(2)) <= 0;
        } else if (version.startsWith("<")) {
            return compareVersions(actualVersion, version.substring(1)) < 0;
        } else if (version.startsWith("=")) {
            return actualVersion.equals(version.substring(1));
        } else {
            return actualVersion.equals(version);
        }
    }
    
    /**
     * 比较两个版本号
     * 
     * @param v1 版本 1
     * @param v2 版本 2
     * @return v1 > v2 返回正数，v1 < v2 返回负数，相等返回 0
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int len = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < len; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        
        return 0;
    }
    
    @Override
    public String toString() {
        return pluginId + " " + version + (optional ? " (optional)" : "");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PluginDependency that = (PluginDependency) o;
        return pluginId.equals(that.pluginId);
    }
    
    @Override
    public int hashCode() {
        return pluginId.hashCode();
    }
}
