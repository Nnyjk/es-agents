package com.easystation.plugin.core.impl;

import com.easystation.plugin.core.PluginDescriptor;

import java.util.List;
import java.util.Map;

/**
 * 插件描述符实现
 */
public class PluginDescriptorImpl implements PluginDescriptor {
    
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final String author;
    private final String license;
    private final String mainClass;
    private final List<PluginDependency> dependencies;
    private final List<String> provides;
    private final List<String> requires;
    private final Map<String, Object> configSchema;
    private final Map<String, Object> configDefaults;
    
    public PluginDescriptorImpl(
            String id,
            String name,
            String version,
            String description,
            String author,
            String license,
            String mainClass,
            List<PluginDependency> dependencies,
            List<String> provides,
            List<String> requires,
            Map<String, Object> configSchema,
            Map<String, Object> configDefaults) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.license = license;
        this.mainClass = mainClass;
        this.dependencies = dependencies;
        this.provides = provides;
        this.requires = requires;
        this.configSchema = configSchema;
        this.configDefaults = configDefaults;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getAuthor() {
        return author;
    }
    
    @Override
    public String getLicense() {
        return license;
    }
    
    @Override
    public String getMainClass() {
        return mainClass;
    }
    
    @Override
    public List<PluginDependency> getDependencies() {
        return dependencies;
    }
    
    @Override
    public List<String> getProvides() {
        return provides;
    }
    
    @Override
    public List<String> getRequires() {
        return requires;
    }
    
    @Override
    public Map<String, Object> getConfigSchema() {
        return configSchema;
    }
    
    @Override
    public Map<String, Object> getConfigDefaults() {
        return configDefaults;
    }
    
    @Override
    public String toString() {
        return "PluginDescriptor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
