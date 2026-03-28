package com.easystation.alert.enums;

/**
 * 告警事件类型
 */
public enum AlertEventType {
    AGENT_OFFLINE,          // Agent 离线
    AGENT_ONLINE,           // Agent 上线
    AGENT_ERROR,            // Agent 错误状态
    AGENT_DEPLOYED,         // Agent 部署完成
    AGENT_HEARTBEAT_MISS,   // Agent 心跳丢失
    AGENT_DEPLOY_FAIL,      // Agent 部署失败
    AGENT_START_FAIL,       // Agent 启动失败
    HOST_OFFLINE,           // 主机离线
    HOST_RESOURCE_HIGH,     // 主机资源使用率高
    TASK_TIMEOUT,           // 任务超时
    TASK_FAIL,              // 任务失败
    CUSTOM                  // 自定义
}