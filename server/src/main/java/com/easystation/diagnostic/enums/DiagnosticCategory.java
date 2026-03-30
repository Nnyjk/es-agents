package com.easystation.diagnostic.enums;

/**
 * 诊断类别
 */
public enum DiagnosticCategory {
    PERFORMANCE("性能", "性能相关问题"),
    SECURITY("安全", "安全配置问题"),
    CONFIGURATION("配置", "系统配置问题"),
    RESOURCE("资源", "资源使用问题"),
    AVAILABILITY("可用性", "服务可用性问题");
    
    private final String label;
    private final String description;
    
    DiagnosticCategory(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
