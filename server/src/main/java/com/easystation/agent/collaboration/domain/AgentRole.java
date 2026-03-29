package com.easystation.agent.collaboration.domain;

/**
 * Agent 角色枚举
 */
public enum AgentRole {
    /**
     * 规划 Agent - 负责任务规划和协调
     */
    PLAN_AGENT("Plan Agent", "负责任务规划、Issue 创建、优先级决策"),
    
    /**
     * 代码执行 Agent - 负责代码实现
     */
    CODE_AGENT("Code Agent", "负责代码实现、测试、PR 提交"),
    
    /**
     * 审查 Agent - 负责代码审查
     */
    REVIEW_AGENT("Review Agent", "负责 PR 审查、反馈、合并"),
    
    /**
     * 通用 Agent - 其他类型的 Agent
     */
    GENERIC_AGENT("Generic Agent", "通用类型的 Agent");

    private final String displayName;
    private final String description;

    AgentRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
