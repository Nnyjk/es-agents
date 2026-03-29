package com.easystation.agent.tool.domain;

/**
 * 工具执行状态枚举
 */
public enum ToolExecutionStatus {
    PENDING,    // 等待执行
    RUNNING,    // 执行中
    SUCCESS,    // 执行成功
    FAILED,     // 执行失败
    TIMEOUT     // 执行超时
}
