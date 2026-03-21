package com.easystation.alert.enums;

/**
 * 告警状态
 */
public enum AlertStatus {
    PENDING,    // 待处理
    NOTIFIED,   // 已通知
    ACKNOWLEDGED, // 已确认
    RESOLVED,   // 已解决
    IGNORED     // 已忽略
}