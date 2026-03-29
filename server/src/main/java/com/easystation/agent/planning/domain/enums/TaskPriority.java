package com.easystation.agent.planning.domain.enums;

/**
 * 任务优先级枚举
 */
public enum TaskPriority {
    /** 低优先级 */
    LOW(1),
    /** 普通优先级 */
    NORMAL(5),
    /** 高优先级 */
    HIGH(10),
    /** 紧急优先级 */
    URGENT(20);

    private final int value;

    TaskPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskPriority fromValue(int value) {
        if (value >= 20) return URGENT;
        if (value >= 10) return HIGH;
        if (value >= 5) return NORMAL;
        return LOW;
    }
}