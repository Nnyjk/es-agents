package com.easystation.build.enums;

/**
 * 构建状态
 */
public enum BuildStatus {
    PENDING,        // 待执行
    RUNNING,        // 执行中
    SUCCESS,        // 成功
    FAILED,         // 失败
    CANCELLED       // 已取消
}