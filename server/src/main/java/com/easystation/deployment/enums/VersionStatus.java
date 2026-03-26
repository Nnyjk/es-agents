package com.easystation.deployment.enums;

/**
 * 版本状态枚举
 */
public enum VersionStatus {
    /**
     * 部署中
     */
    DEPLOYING,
    
    /**
     * 当前版本
     */
    CURRENT,
    
    /**
     * 历史版本
     */
    HISTORY,
    
    /**
     * 已回滚
     */
    ROLLED_BACK,
    
    /**
     * 标记为稳定版本
     */
    STABLE,
    
    /**
     * 标记为问题版本
     */
    PROBLEMATIC
}