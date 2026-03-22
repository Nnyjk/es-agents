package com.easystation.agent.domain.enums;

/**
 * Agent模板分类
 */
public enum TemplateCategory {
    MONITORING("监控"),
    DEPLOYMENT("部署"),
    BACKUP("备份"),
    SECURITY("安全"),
    DATABASE("数据库"),
    NETWORK("网络"),
    UTILITY("工具"),
    CUSTOM("自定义");

    private final String description;

    TemplateCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}