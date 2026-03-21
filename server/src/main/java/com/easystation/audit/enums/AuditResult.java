package com.easystation.audit.enums;

/**
 * 审计操作结果
 */
public enum AuditResult {
    SUCCESS("成功"),
    FAILED("失败"),
    PARTIAL("部分成功");

    private final String description;

    AuditResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}