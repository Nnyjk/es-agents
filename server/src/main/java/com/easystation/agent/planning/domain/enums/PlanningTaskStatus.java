package com.easystation.agent.planning.domain.enums;

/**
 * 任务状态枚举
 * 定义任务规划中的任务状态机
 */
public enum PlanningTaskStatus {
    /** 任务已创建，等待分解 */
    CREATED,
    /** 任务正在分解 */
    DECOMPOSING,
    /** 任务已分解，等待调度 */
    READY,
    /** 任务已调度，等待执行 */
    SCHEDULED,
    /** 任务正在执行 */
    RUNNING,
    /** 任务执行成功 */
    COMPLETED,
    /** 任务执行失败 */
    FAILED,
    /** 任务已取消 */
    CANCELLED,
    /** 任务已暂停 */
    PAUSED,
    /** 任务正在重试 */
    RETRYING
}