package com.easystation.plugin.sandbox.service;

import com.easystation.plugin.sandbox.domain.SecurityContext;
import com.easystation.plugin.sandbox.domain.PluginSandbox;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 资源监控器
 */
@ApplicationScoped
public class ResourceMonitor {
    
    private static final Logger LOG = Logger.getLogger(ResourceMonitor.class);
    
    @Inject
    PluginSandbox pluginSandbox;
    
    private final Map<String, ResourceStats> stats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public ResourceMonitor() {
        scheduler.scheduleAtFixedRate(this::monitorAllResources, 10, 10, TimeUnit.SECONDS);
    }
    
    public void recordUsage(String pluginId, ResourceType type, long value) {
        stats.computeIfAbsent(pluginId, k -> new ResourceStats(k)).record(type, value);
    }
    
    public ResourceStats getStats(String pluginId) {
        return stats.get(pluginId);
    }
    
    private void monitorAllResources() {
        stats.forEach((pluginId, stat) -> {
            try {
                SecurityContext context = pluginSandbox.getContext(pluginId);
                long memoryUsed = context.getMemoryUsed();
                long memoryLimit = context.getResourceLimit().maxMemory();
                double memoryUsage = (double) memoryUsed / memoryLimit * 100;
                
                if (memoryUsage > 80) {
                    LOG.warnf("High memory usage for plugin %s: %.1f%%", pluginId, memoryUsage);
                }
                
                long remainingTime = context.getRemainingExecutionTime();
                if (remainingTime < 5000) {
                    LOG.warnf("Low remaining execution time for plugin %s: %dms", pluginId, remainingTime);
                }
            } catch (IllegalStateException e) {
                stats.remove(pluginId);
            }
        });
    }
    
    public void cleanup(String pluginId) {
        stats.remove(pluginId);
    }
    
    public Map<String, ResourceStats> getAllStats() {
        return Map.copyOf(stats);
    }
    
    public static class ResourceStats {
        private final String pluginId;
        private final Map<ResourceType, Long> usage = new ConcurrentHashMap<>();
        
        public ResourceStats(String pluginId) {
            this.pluginId = pluginId;
        }
        
        public void record(ResourceType type, long value) {
            usage.merge(type, value, Long::sum);
        }
        
        public long getUsage(ResourceType type) {
            return usage.getOrDefault(type, 0L);
        }
        
        public String getPluginId() {
            return pluginId;
        }
    }
    
    public enum ResourceType {
        MEMORY, CPU_TIME, FILE_OPERATIONS, NETWORK_CALLS, EXECUTION_TIME
    }
}
