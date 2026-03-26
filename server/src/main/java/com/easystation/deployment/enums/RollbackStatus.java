package com.easystation.deployment.enums;

/**
 * 回滚状态枚举
 */
public enum RollbackStatus {
    /**
     * 待执行
     */
    PENDING,
    
    /**
     * 预检中
     */
    PRECHECKING,
    
    /**
     * 预检通过
     */
    PRECHECK_PASSED,
    
    /**
     * 预检失败
     */
    PRECHECK_FAILED,
    
    /**
     * 执行中
     */
    EXECUTING,
    
    /**
     * 验证中
     */
    VERIFYING,
    
    /**
     * 成功
     */
    SUCCESS,
    
    /**
     * 失败
     */
    FAILED,
    
    /**
     * 已取消
     */
    CANCELLED
}