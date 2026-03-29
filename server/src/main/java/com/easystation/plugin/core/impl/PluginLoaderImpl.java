package com.easystation.plugin.core.impl;

import com.easystation.plugin.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件加载器基础实现
 * 
 * 提供插件加载、初始化、启动、停止的基本实现。
 */
public class PluginLoaderImpl implements PluginLoader {
    
    private static final Logger log = LoggerFactory.getLogger(PluginLoaderImpl.class);
    
    /**
     * 已加载的插件注册表
     */
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    
    /**
     * 插件上下文工厂
     */
    private final PluginContextFactory contextFactory;
    
    /**
     * 扩展注册表
     */
    private final ExtensionRegistry extensionRegistry;
    
    public PluginLoaderImpl(PluginContextFactory contextFactory, ExtensionRegistry extensionRegistry) {
        this.contextFactory = contextFactory;
        this.extensionRegistry = extensionRegistry;
    }
    
    @Override
    public List<PluginDescriptor> scan(Path pluginDir) {
        log.info("Scanning plugin directory: {}", pluginDir);
        // TODO: 实现插件目录扫描
        return new ArrayList<>();
    }
    
    @Override
    public Plugin load(PluginDescriptor descriptor) throws PluginException {
        String pluginId = descriptor.getId();
        
        if (plugins.containsKey(pluginId)) {
            throw new PluginException(pluginId, null, "Plugin already loaded: " + pluginId);
        }
        
        log.info("Loading plugin: {} v{}", descriptor.getName(), descriptor.getVersion());
        
        try {
            // TODO: 加载插件类
            // Class<?> pluginClass = loadPluginClass(descriptor);
            // Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
            
            // 临时返回 null，待实现
            return null;
        } catch (Exception e) {
            throw new PluginException(pluginId, PluginState.NEW, "Failed to load plugin", e);
        }
    }
    
    @Override
    public void initialize(Plugin plugin, PluginContext context) throws PluginException {
        String pluginId = plugin.getDescriptor().getId();
        log.info("Initializing plugin: {}", pluginId);
        
        try {
            plugin.initialize(context);
        } catch (Exception e) {
            throw new PluginException(pluginId, PluginState.INITIALIZING, "Failed to initialize plugin", e);
        }
    }
    
    @Override
    public void start(Plugin plugin) throws PluginException {
        String pluginId = plugin.getDescriptor().getId();
        log.info("Starting plugin: {}", pluginId);
        
        try {
            plugin.start();
        } catch (Exception e) {
            throw new PluginException(pluginId, PluginState.STARTING, "Failed to start plugin", e);
        }
    }
    
    @Override
    public void stop(Plugin plugin) {
        String pluginId = plugin.getDescriptor().getId();
        log.info("Stopping plugin: {}", pluginId);
        
        try {
            plugin.stop();
        } catch (Exception e) {
            log.error("Error stopping plugin: {}", pluginId, e);
        }
    }
    
    @Override
    public void unload(Plugin plugin) {
        String pluginId = plugin.getDescriptor().getId();
        log.info("Unloading plugin: {}", pluginId);
        
        plugins.remove(pluginId);
        // TODO: 清理插件资源
    }
    
    @Override
    public Optional<Plugin> getPlugin(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }
    
    @Override
    public List<Plugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    @Override
    public List<Plugin> getPluginsByState(PluginState state) {
        List<Plugin> result = new ArrayList<>();
        for (Plugin plugin : plugins.values()) {
            if (plugin.getState() == state) {
                result.add(plugin);
            }
        }
        return result;
    }
    
    /**
     * 注册插件
     */
    protected void registerPlugin(Plugin plugin) {
        plugins.put(plugin.getDescriptor().getId(), plugin);
    }
}
