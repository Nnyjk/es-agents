package com.easystation.agent.memory.domain;

/**
 * 记忆类型
 */
public enum MemoryType {
    /** 短期记忆 - 对话上下文，自动过期 */
    SHORT_TERM,
    
    /** 长期记忆 - 持久化存储，支持检索 */
    LONG_TERM
}
