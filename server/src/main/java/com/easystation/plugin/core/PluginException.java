package com.easystation.plugin.core;

/**
 * 插件异常
 * 
 * 插件操作过程中抛出的异常。
 */
public class PluginException extends Exception {
    
    /**
     * 插件 ID
     */
    private final String pluginId;
    
    /**
     * 插件状态
     */
    private final PluginState state;
    
    /**
     * 构造方法
     * @param message 异常消息
     */
    public PluginException(String message) {
        super(message);
        this.pluginId = null;
        this.state = null;
    }
    
    /**
     * 构造方法
     * @param message 异常消息
     * @param cause 原因
     */
    public PluginException(String message, Throwable cause) {
        super(message, cause);
        this.pluginId = null;
        this.state = null;
    }
    
    /**
     * 构造方法
     * @param pluginId 插件 ID
     * @param state 插件状态
     * @param message 异常消息
     */
    public PluginException(String pluginId, PluginState state, String message) {
        super(message);
        this.pluginId = pluginId;
        this.state = state;
    }
    
    /**
     * 构造方法
     * @param pluginId 插件 ID
     * @param state 插件状态
     * @param message 异常消息
     * @param cause 原因
     */
    public PluginException(String pluginId, PluginState state, String message, Throwable cause) {
        super(message, cause);
        this.pluginId = pluginId;
        this.state = state;
    }
    
    /**
     * 获取插件 ID
     * @return 插件 ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * 获取插件状态
     * @return 插件状态
     */
    public PluginState getState() {
        return state;
    }
}
