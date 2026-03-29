package com.easystation.plugin.core;

/**
 * 插件运行时状态
 * 
 * 状态机流转:
 * NEW → INITIALIZING → INITIALIZED → STARTING → ACTIVE → STOPPING → STOPPED
 *                                     ↓
 *                                  FAILED
 */
public enum PluginState {
    /** 刚创建，未初始化 */
    NEW,
    
    /** 初始化中 */
    INITIALIZING,
    
    /** 已初始化，未启动 */
    INITIALIZED,
    
    /** 启动中 */
    STARTING,
    
    /** 运行中 */
    ACTIVE,
    
    /** 停止中 */
    STOPPING,
    
    /** 已停止 */
    STOPPED,
    
    /** 失败 */
    FAILED
}
