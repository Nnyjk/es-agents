package com.easystation.plugin.sandbox.domain;

import com.easystation.plugin.sandbox.exception.ResourceLimitExceededException;
import com.easystation.plugin.sandbox.exception.SecurityViolationException;
import com.easystation.plugin.sandbox.exception.TimeoutException;
import com.easystation.plugin.sandbox.service.SecurityChecker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 插件沙箱实现
 */
@ApplicationScoped
public class PluginSandboxImpl implements PluginSandbox {
    
    private static final Logger LOG = Logger.getLogger(PluginSandboxImpl.class);
    
    private final Map<String, SecurityContext> contexts = new ConcurrentHashMap<>();
    
    @Inject
    SecurityChecker securityChecker;
    
    @Override
    public SecurityContext createContext(String pluginId, List<Permission> permissions, ResourceLimit resourceLimit) {
        LOG.infof("Creating security context for plugin: %s", pluginId);
        SecurityContext context = new SecurityContext(pluginId, permissions, resourceLimit);
        contexts.put(pluginId, context);
        return context;
    }
    
    @Override
    public SecurityContext getContext(String pluginId) {
        SecurityContext context = contexts.get(pluginId);
        if (context == null) {
            throw new IllegalStateException("No security context found for plugin: " + pluginId);
        }
        return context;
    }
    
    @Override
    public void removeContext(String pluginId) {
        SecurityContext removed = contexts.remove(pluginId);
        if (removed != null) {
            LOG.infof("Removed security context for plugin: %s", pluginId);
        }
    }
    
    @Override
    public void checkPermission(SecurityContext context, PermissionType type, String target) {
        if (!context.hasPermission(type, target)) {
            throw new SecurityViolationException(
                String.format("Plugin %s does not have permission for %s on %s", 
                    context.getPluginId(), type, target)
            );
        }
    }
    
    @Override
    public <T> T execute(SecurityContext context, Supplier<T> action) {
        // 检查执行时间
        if (!context.checkExecutionTime()) {
            throw new TimeoutException(
                String.format("Plugin %s execution timed out", context.getPluginId())
            );
        }
        
        try {
            return action.get();
        } catch (SecurityViolationException | ResourceLimitExceededException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            LOG.warnf("Exception during plugin execution: %s", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void executeVoid(SecurityContext context, Runnable action) {
        execute(context, () -> {
            action.run();
            return null;
        });
    }
}
