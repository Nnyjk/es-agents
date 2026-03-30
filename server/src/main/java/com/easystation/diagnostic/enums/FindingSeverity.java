package com.easystation.diagnostic.enums;

/**
 * 发现严重程度
 */
public enum FindingSeverity {
    INFO("信息", 1),
    WARNING("警告", 2),
    CRITICAL("严重", 3),
    FATAL("致命", 4);
    
    private final String label;
    private final int level;
    
    FindingSeverity(String label, int level) {
        this.label = label;
        this.level = level;
    }
    
    public String getLabel() { return label; }
    public int getLevel() { return level; }
}
