package com.easystation.plugin.domain.enums;

public enum ReviewStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已驳回");

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}