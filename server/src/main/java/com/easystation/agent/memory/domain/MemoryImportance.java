package com.easystation.agent.memory.domain;

/**
 * 记忆重要性等级
 */
public enum MemoryImportance {
    /** 低重要性 - 可快速淘汰 */
    LOW,
    
    /** 中等重要性 - 正常保留 */
    MEDIUM,
    
    /** 高重要性 - 优先保留 */
    HIGH,
    
    /** 关键记忆 - 永久保留 */
    CRITICAL
}
