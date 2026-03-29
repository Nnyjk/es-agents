package com.easystation.plugin.core;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * 插件描述符解析器
 * 
 * 从 plugin.json 文件解析插件描述符。
 */
public interface PluginDescriptorParser {
    
    /**
     * 从输入流解析插件描述符
     * @param inputStream plugin.json 输入流
     * @return 插件描述符
     * @throws PluginException 解析失败时抛出
     */
    PluginDescriptor parse(InputStream inputStream) throws PluginException;
    
    /**
     * 从文件路径解析插件描述符
     * @param pluginJsonPath plugin.json 文件路径
     * @return 插件描述符
     * @throws PluginException 解析失败时抛出
     */
    PluginDescriptor parse(Path pluginJsonPath) throws PluginException;
}
