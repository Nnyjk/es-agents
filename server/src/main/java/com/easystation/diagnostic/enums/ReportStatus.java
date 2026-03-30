package com.easystation.diagnostic.enums;

/**
 * 报告状态
 */
public enum ReportStatus {
    GENERATING("生成中"),
    COMPLETED("已完成"),
    FAILED("失败");
    
    private final String label;
    
    ReportStatus(String label) {
        this.label = label;
    }
    
    public String getLabel() { return label; }
}
