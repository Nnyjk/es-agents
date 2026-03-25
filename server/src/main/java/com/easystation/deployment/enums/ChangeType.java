package com.easystation.deployment.enums;

/**
 * 变更类型枚举
 */
public enum ChangeType {
    /**
     * 代码变更
     */
    CODE,
    
    /**
     * 配置变更
     */
    CONFIG,
    
    /**
     * 依赖变更
     */
    DEPENDENCY,
    
    /**
     * 环境变量变更
     */
    ENVIRONMENT,
    
    /**
     * 资源变更
     */
    RESOURCE,
    
    /**
     * 数据库变更
     */
    DATABASE
}