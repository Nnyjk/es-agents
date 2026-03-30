package com.easystation.plugin.loader;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.nio.file.Path;
import java.util.Optional;

/**
 * PluginManager CDI Producer
 * 
 * Provides PluginManager as a CDI bean for dependency injection.
 */
@ApplicationScoped
public class PluginManagerProducer {
    
    private static final Logger LOG = Logger.getLogger(PluginManagerProducer.class);
    
    @ConfigProperty(name = "plugins.directory", defaultValue = "plugins")
    String pluginsDirectory;
    
    private PluginManager pluginManager;
    
    @Produces
    @ApplicationScoped
    public PluginManager producePluginManager() {
        if (pluginManager == null) {
            Path pluginsDir = Path.of(pluginsDirectory);
            pluginManager = new PluginManager(pluginsDir);
            LOG.infof("PluginManager created with plugins directory: %s", pluginsDir.toAbsolutePath());
        }
        return pluginManager;
    }
    
    @PreDestroy
    public void destroy() {
        if (pluginManager != null) {
            try {
                pluginManager.shutdown();
                LOG.info("PluginManager shutdown completed");
            } catch (Exception e) {
                LOG.errorf(e, "Error during PluginManager shutdown");
            }
        }
    }
}