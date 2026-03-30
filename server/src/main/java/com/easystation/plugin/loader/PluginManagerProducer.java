package com.easystation.plugin.loader;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * CDI Producer for PluginManager
 * 
 * Creates and manages the PluginManager lifecycle as a CDI bean.
 */
@ApplicationScoped
public class PluginManagerProducer {
    
    private static final Logger log = LoggerFactory.getLogger(PluginManagerProducer.class);
    
    private PluginManager pluginManager;
    
    @Produces
    @ApplicationScoped
    public PluginManager producePluginManager(
            @ConfigProperty(name = "plugin.directory", defaultValue = "plugins") 
            String pluginDirectory) {
        
        Path pluginsPath = Path.of(pluginDirectory);
        log.info("Initializing PluginManager with directory: {}", pluginsPath.toAbsolutePath());
        
        this.pluginManager = new PluginManager(pluginsPath);
        return pluginManager;
    }
    
    @PreDestroy
    public void destroy() {
        if (pluginManager != null) {
            log.info("Shutting down PluginManager");
            // PluginManager will handle its own cleanup
        }
    }
}