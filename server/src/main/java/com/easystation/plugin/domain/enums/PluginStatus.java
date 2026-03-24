package com.easystation.plugin.domain.enums;

public enum PluginStatus {
    DRAFT("草稿"),
    PENDING("待审核"),
    PENDING_REVIEW("待审核"),
    APPROVED("已通过"),
    REJECTED("已驳回"),
    PUBLISHED("已发布"),
    DEPRECATED("已废弃"),
    REMOVED("已下架"),
    SUSPENDED("已暂停"),
    DELETED("已删除");

    private final String description;

    PluginStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}