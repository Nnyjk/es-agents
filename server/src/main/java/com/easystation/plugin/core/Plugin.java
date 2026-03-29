package com.easystation.plugin.core;

/**
 * 插件核心接口
 * 
 * 所有插件必须实现此接口，定义插件的生命周期方法。
 */
public interface Plugin {
    
    /**
     * 获取插件描述符
     * @return 插件描述符
     */
    PluginDescriptor getDescriptor();
    
    /**
     * 初始化插件
     * 在插件加载时调用，用于注册扩展点、加载配置等
     * @param context 插件上下文
     * @throws PluginException 初始化失败时抛出
     */
    void initialize(PluginContext context) throws PluginException;
    
    /**
     * 启动插件
     * 在初始化完成后调用，用于启动服务、注册监听器等
     * @throws PluginException 启动失败时抛出
     */
    void start() throws PluginException;
    
    /**
     * 停止插件
     * 在插件卸载时调用，用于释放资源、注销监听器等
     */
    void stop();
    
    /**
     * 获取插件当前状态
     * @return 插件状态
     */
    PluginState getState();
}
