package com.easystation.plugin.sandbox.service;

import com.easystation.plugin.sandbox.domain.SecurityContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 超时管理器
 */
@ApplicationScoped
public class TimeoutManager {
    
    private static final Logger LOG = Logger.getLogger(TimeoutManager.class);
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
    
    public void registerTimeout(String pluginId, SecurityContext context, long timeoutMillis, Runnable onTimeout) {
        LOG.infof("Registering timeout for plugin %s: %dms", pluginId, timeoutMillis);
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            if (!context.checkExecutionTime()) {
                LOG.warnf("Plugin %s timed out after %dms", pluginId, timeoutMillis);
                if (onTimeout != null) {
                    onTimeout.run();
                }
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);
        
        timeoutTasks.put(pluginId, future);
    }
    
    public void cancelTimeout(String pluginId) {
        ScheduledFuture<?> future = timeoutTasks.remove(pluginId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }
    
    public boolean isTimedOut(String pluginId, SecurityContext context) {
        return !context.checkExecutionTime();
    }
    
    public long getRemainingTime(String pluginId, SecurityContext context) {
        return context.getRemainingExecutionTime();
    }
    
    public void cleanup() {
        timeoutTasks.forEach((pluginId, future) -> {
            if (!future.isDone()) {
                future.cancel(false);
            }
        });
        timeoutTasks.clear();
    }
    
    public int getActiveTaskCount() {
        return (int) timeoutTasks.values().stream().filter(f -> !f.isDone()).count();
    }
}
