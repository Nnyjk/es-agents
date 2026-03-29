package com.easystation.plugin.core.impl;

import com.easystation.plugin.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展注册表基础实现
 */
public class ExtensionRegistryImpl implements ExtensionRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ExtensionRegistryImpl.class);
    
    /**
     * 扩展点注册表：name -> ExtensionPoint
     */
    private final Map<String, ExtensionPoint<?>> extensionPoints = new ConcurrentHashMap<>();
    
    /**
     * 扩展实现注册表：extensionPointName -> List<ExtensionWrapper>
     */
    private final Map<String, List<ExtensionWrapper<?>>> extensions = new ConcurrentHashMap<>();
    
    @Override
    public <T> void registerExtensionPoint(ExtensionPoint<T> extensionPoint, Plugin plugin) {
        String name = extensionPoint.getName();
        log.debug("Registering extension point: {} from plugin: {}", name, plugin.getDescriptor().getId());
        
        extensionPoints.put(name, extensionPoint);
        extensions.computeIfAbsent(name, k -> new ArrayList<>());
    }
    
    @Override
    public void unregisterExtensionPoint(String name, Plugin plugin) {
        log.debug("Unregistering extension point: {} from plugin: {}", name, plugin.getDescriptor().getId());
        extensionPoints.remove(name);
        extensions.remove(name);
    }
    
    @Override
    public <T> void registerExtension(String extensionPoint, T extension, Plugin plugin) {
        log.debug("Registering extension: {} from plugin: {}", extensionPoint, plugin.getDescriptor().getId());
        
        List<ExtensionWrapper<?>> list = extensions.computeIfAbsent(extensionPoint, k -> new ArrayList<>());
        list.add(new ExtensionWrapper<>(extension, plugin));
    }
    
    @Override
    public <T> void unregisterExtension(String extensionPoint, T extension, Plugin plugin) {
        log.debug("Unregistering extension: {} from plugin: {}", extensionPoint, plugin.getDescriptor().getId());
        
        List<ExtensionWrapper<?>> list = extensions.get(extensionPoint);
        if (list != null) {
            list.removeIf(w -> w.getExtension() == extension && w.getPlugin() == plugin);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ExtensionPoint<T>> getExtensionPoint(String name) {
        ExtensionPoint<?> point = extensionPoints.get(name);
        return point != null ? Optional.of((ExtensionPoint<T>) point) : Optional.empty();
    }
    
    @Override
    public List<ExtensionPoint<?>> getAllExtensionPoints() {
        return new ArrayList<>(extensionPoints.values());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getExtensions(String extensionPoint) {
        List<ExtensionWrapper<?>> list = extensions.get(extensionPoint);
        if (list == null) {
            return new ArrayList<>();
        }
        
        List<T> result = new ArrayList<>();
        for (ExtensionWrapper<?> wrapper : list) {
            result.add((T) wrapper.getExtension());
        }
        return result;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getExtensionsByPlugin(String extensionPoint, Plugin plugin) {
        List<ExtensionWrapper<?>> list = extensions.get(extensionPoint);
        if (list == null) {
            return new ArrayList<>();
        }
        
        List<T> result = new ArrayList<>();
        for (ExtensionWrapper<?> wrapper : list) {
            if (wrapper.getPlugin() == plugin) {
                result.add((T) wrapper.getExtension());
            }
        }
        return result;
    }
    
    @Override
    public List<Object> getAllExtensionsByPlugin(Plugin plugin) {
        List<Object> result = new ArrayList<>();
        for (List<ExtensionWrapper<?>> list : extensions.values()) {
            for (ExtensionWrapper<?> wrapper : list) {
                if (wrapper.getPlugin() == plugin) {
                    result.add(wrapper.getExtension());
                }
            }
        }
        return result;
    }
    
    /**
     * 扩展包装器
     */
    private static class ExtensionWrapper<T> {
        private final T extension;
        private final Plugin plugin;
        
        public ExtensionWrapper(T extension, Plugin plugin) {
            this.extension = extension;
            this.plugin = plugin;
        }
        
        public T getExtension() {
            return extension;
        }
        
        public Plugin getPlugin() {
            return plugin;
        }
    }
}
