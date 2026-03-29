package com.easystation.plugin.core;

import java.util.List;
import java.util.Map;

/**
 * 插件描述符
 * 
 * 定义插件的元数据信息，通常从 plugin.json 加载。
 */
public interface PluginDescriptor {
    
    /**
     * 获取插件唯一标识
     * @return 插件 ID
     */
    String getId();
    
    /**
     * 获取插件名称
     * @return 插件名称
     */
    String getName();
    
    /**
     * 获取插件版本
     * @return 版本号 (语义化版本格式：major.minor.patch)
     */
    String getVersion();
    
    /**
     * 获取插件描述
     * @return 插件描述
     */
    String getDescription();
    
    /**
     * 获取插件作者
     * @return 作者信息
     */
    String getAuthor();
    
    /**
     * 获取插件许可证
     * @return 许可证名称
     */
    String getLicense();
    
    /**
     * 获取插件主类名
     * @return 主类全限定名
     */
    String getMainClass();
    
    /**
     * 获取插件依赖列表
     * @return 依赖列表
     */
    List<PluginDependency> getDependencies();
    
    /**
     * 获取插件提供的扩展点
     * @return 提供的扩展点列表
     */
    List<String> getProvides();
    
    /**
     * 获取插件需要的扩展点
     * @return 需要的扩展点列表
     */
    List<String> getRequires();
    
    /**
     * 获取插件配置 Schema
     * @return 配置 Schema
     */
    Map<String, Object> getConfigSchema();
    
    /**
     * 获取插件默认配置
     * @return 默认配置
     */
    Map<String, Object> getConfigDefaults();
    
    /**
     * 插件依赖
     */
    class PluginDependency {
        private final String id;
        private final String version;
        
        public PluginDependency(String id, String version) {
            this.id = id;
            this.version = version;
        }
        
        public String getId() {
            return id;
        }
        
        public String getVersion() {
            return version;
        }
        
        /**
         * 检查版本是否匹配
         * @param targetVersion 目标版本
         * @return 是否匹配
         */
        public boolean isVersionCompatible(String targetVersion) {
            // 简单实现：支持 >=, >, <=, <, = 前缀
            if (version.startsWith(">=")) {
                return compareVersions(targetVersion, version.substring(2)) >= 0;
            } else if (version.startsWith(">")) {
                return compareVersions(targetVersion, version.substring(1)) > 0;
            } else if (version.startsWith("<=")) {
                return compareVersions(targetVersion, version.substring(2)) <= 0;
            } else if (version.startsWith("<")) {
                return compareVersions(targetVersion, version.substring(1)) < 0;
            } else {
                return version.equals(targetVersion) || version.startsWith("=");
            }
        }
        
        /**
         * 比较两个语义化版本
         * @param v1 版本 1
         * @param v2 版本 2
         * @return v1 > v2 返回正数，v1 < v2 返回负数，相等返回 0
         */
        private int compareVersions(String v1, String v2) {
            String[] parts1 = v1.split("\\.");
            String[] parts2 = v2.split("\\.");
            
            for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
                int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
                
                if (num1 != num2) {
                    return num1 - num2;
                }
            }
            
            return 0;
        }
    }
}
