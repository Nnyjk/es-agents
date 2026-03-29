package com.easystation.plugin.core.impl;

import com.easystation.plugin.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 插件上下文基础实现
 */
public class PluginContextImpl implements PluginContext {
    
    private static final Logger log = LoggerFactory.getLogger(PluginContextImpl.class);
    
    private final PluginDescriptor descriptor;
    private final Plugin plugin;
    private final Path dataDirectory;
    private final Path configDirectory;
    private final Path logDirectory;
    private final Map<String, String> config;
    private final Map<Class<?>, Object> services;
    private final ExtensionRegistry extensionRegistry;
    private final ClassLoader classLoader;
    private volatile boolean active;
    
    public PluginContextImpl(
            PluginDescriptor descriptor,
            Plugin plugin,
            Path baseDirectory,
            Map<String, String> config,
            ExtensionRegistry extensionRegistry,
            ClassLoader classLoader) {
        this.descriptor = descriptor;
        this.plugin = plugin;
        this.dataDirectory = baseDirectory.resolve("data").resolve(descriptor.getId());
        this.configDirectory = baseDirectory.resolve("config").resolve(descriptor.getId());
        this.logDirectory = baseDirectory.resolve("logs").resolve(descriptor.getId());
        this.config = new HashMap<>(config);
        this.services = new HashMap<>();
        this.extensionRegistry = extensionRegistry;
        this.classLoader = classLoader;
        this.active = false;
        
        // 确保目录存在
        ensureDirectory(this.dataDirectory);
        ensureDirectory(this.configDirectory);
        ensureDirectory(this.logDirectory);
    }
    
    private void ensureDirectory(Path path) {
        File dir = path.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }
    
    @Override
    public String getDataDirectory() {
        return dataDirectory.toString();
    }
    
    @Override
    public String getConfigDirectory() {
        return configDirectory.toString();
    }
    
    @Override
    public String getLogDirectory() {
        return logDirectory.toString();
    }
    
    @Override
    public Optional<String> getConfig(String key) {
        return Optional.ofNullable(config.get(key));
    }
    
    @Override
    public String getConfig(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }
    
    @Override
    public <T> void registerService(Class<T> serviceClass, T service) {
        services.put(serviceClass, service);
        log.debug("Service registered: {}", serviceClass.getName());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getExtensions(String extensionPoint) {
        return extensionRegistry.getExtensions(extensionPoint);
    }
    
    @Override
    public void registerExtension(String extensionPoint, Object extension) {
        extensionRegistry.registerExtension(extensionPoint, extension, plugin);
        log.debug("Extension registered: {} -> {}", extensionPoint, extension.getClass().getName());
    }
    
    @Override
    public void publishEvent(Object event) {
        // TODO: 实现事件发布机制
        log.debug("Event published: {}", event.getClass().getSimpleName());
    }
    
    @Override
    public Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
    
    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    /**
     * 设置激活状态
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
