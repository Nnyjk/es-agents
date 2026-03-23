package com.easystation.plugin.domain.enums;

public enum PluginStatus {
    DRAFT("草稿"),
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已驳回"),
    PUBLISHED("已发布"),
    DEPRECATED("已废弃"),
    REMOVED("已下架");

    private final String description;

    PluginStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}