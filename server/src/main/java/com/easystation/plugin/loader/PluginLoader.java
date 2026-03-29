package com.easystation.plugin.loader;

import com.easystation.plugin.core.Plugin;
import com.easystation.plugin.core.PluginDescriptor;
import com.easystation.plugin.core.PluginException;

import java.util.List;

/**
 * 插件加载器接口
 * 
 * 负责发现和加载插件，包括：
 * - 扫描插件目录
 * - 解析插件描述符
 * - 加载插件类
 */
public interface PluginLoader {
    
    /**
     * 发现所有可用插件
     * 
     * 扫描插件目录，解析所有有效的 plugin.json 文件。
     * 
     * @return 插件描述符列表
     */
    List<PluginDescriptor> discoverPlugins();
    
    /**
     * 加载插件
     * 
     * 根据描述符加载插件类并实例化。
     * 
     * @param descriptor 插件描述符
     * @return 插件实例
     * @throws PluginException 加载失败时抛出
     */
    Plugin loadPlugin(PluginDescriptor descriptor) throws PluginException;
    
    /**
     * 卸载插件
     * 
     * 释放插件占用的资源，包括关闭 ClassLoader。
     * 
     * @param pluginId 插件 ID
     */
    void unloadPlugin(String pluginId);
}
