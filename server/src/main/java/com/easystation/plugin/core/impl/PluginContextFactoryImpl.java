package com.easystation.plugin.core.impl;

import com.easystation.plugin.core.*;

import java.nio.file.Path;
import java.util.Map;

/**
 * 插件上下文工厂实现
 */
public class PluginContextFactoryImpl implements PluginContextFactory {
    
    private final Path baseDirectory;
    private final Map<String, String> globalConfig;
    private final ExtensionRegistry extensionRegistry;
    
    public PluginContextFactoryImpl(
            Path baseDirectory,
            Map<String, String> globalConfig,
            ExtensionRegistry extensionRegistry) {
        this.baseDirectory = baseDirectory;
        this.globalConfig = globalConfig;
        this.extensionRegistry = extensionRegistry;
    }
    
    @Override
    public PluginContext createContext(PluginDescriptor descriptor, Plugin plugin) {
        return new PluginContextImpl(
            descriptor,
            plugin,
            baseDirectory,
            globalConfig,
            extensionRegistry,
            plugin.getClass().getClassLoader()
        );
    }
}
