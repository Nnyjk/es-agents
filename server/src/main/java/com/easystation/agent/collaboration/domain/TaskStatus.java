package com.easystation.agent.collaboration.domain;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    PENDING("pending", "待处理"),
    ASSIGNED("assigned", "已分配"),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败"),
    CANCELLED("cancelled", "取消");

    private final String code;
    private final String description;

    TaskStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
