package com.easystation.deployment.enums;

/**
 * 回滚策略枚举
 */
public enum RollbackStrategy {
    /**
     * 快速回滚 - 直接切换版本
     */
    FAST,
    
    /**
     * 渐进式回滚 - 逐步替换实例
     */
    GRADUAL,
    
    /**
     * 蓝绿回滚 - 切换流量
     */
    BLUE_GREEN
}