package com.easystation.agent.collaboration.domain;

/**
 * 消息类型枚举
 */
public enum MessageType {
    TASK_REQUEST("task_request", "任务请求"),
    TASK_RESPONSE("task_response", "任务响应"),
    STATUS_UPDATE("status_update", "状态更新"),
    COORDINATION("coordination", "协调消息"),
    QUERY_REQUEST("query_request", "查询请求"),
    QUERY_RESPONSE("query_response", "查询响应");

    private final String code;
    private final String description;

    MessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
