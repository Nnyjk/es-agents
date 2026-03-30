package com.easystation.plugin.loader;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.annotation.Priority;

import java.nio.file.Path;

/**
 * Mock PluginManager producer for tests
 *
 * Provides a mock PluginManager that doesn't require actual plugin files.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class TestPluginManagerProducer {

    @Produces
    @ApplicationScoped
    public PluginManager producePluginManager() {
        // Create PluginManager with a test directory that doesn't need real plugins
        Path testPluginsPath = Path.of("target/plugins");
        return new PluginManager(testPluginsPath);
    }
}