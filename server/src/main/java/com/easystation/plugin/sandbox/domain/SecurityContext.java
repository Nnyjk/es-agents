package com.easystation.plugin.sandbox.domain;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全上下文，跟踪资源使用
 */
public class SecurityContext {
    
    private final String pluginId;
    private final List<Permission> permissions;
    private final ResourceLimit resourceLimit;
    private final long startTime;
    private final AtomicLong memoryUsed;
    private final AtomicLong fileOperations;
    private final AtomicLong networkCalls;
    
    public SecurityContext(String pluginId, List<Permission> permissions, ResourceLimit resourceLimit) {
        this.pluginId = pluginId;
        this.permissions = permissions;
        this.resourceLimit = resourceLimit;
        this.startTime = System.currentTimeMillis();
        this.memoryUsed = new AtomicLong(0);
        this.fileOperations = new AtomicLong(0);
        this.networkCalls = new AtomicLong(0);
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    public List<Permission> getPermissions() {
        return permissions;
    }
    
    public ResourceLimit getResourceLimit() {
        return resourceLimit;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getMemoryUsed() {
        return memoryUsed.get();
    }
    
    public long getFileOperations() {
        return fileOperations.get();
    }
    
    public long getNetworkCalls() {
        return networkCalls.get();
    }
    
    public long getExecutionDuration() {
        return System.currentTimeMillis() - startTime;
    }
    
    public long getRemainingExecutionTime() {
        long elapsed = getExecutionDuration();
        long maxMs = resourceLimit.maxExecutionTime() * 1000L;
        return Math.max(0, maxMs - elapsed);
    }
    
    public synchronized boolean checkMemory(long bytes) {
        if (!resourceLimit.checkMemory(memoryUsed.get() + bytes)) {
            return false;
        }
        memoryUsed.addAndGet(bytes);
        return true;
    }
    
    public synchronized boolean checkFileOperation() {
        long count = fileOperations.incrementAndGet();
        return resourceLimit.checkFileOperation((int) count);
    }
    
    public synchronized boolean checkNetworkCall() {
        long count = networkCalls.incrementAndGet();
        return resourceLimit.checkNetworkCall((int) count);
    }
    
    public boolean checkExecutionTime() {
        return getRemainingExecutionTime() > 0;
    }
    
    public boolean hasPermission(PermissionType type, String target) {
        return permissions.stream()
            .filter(p -> p.type() == type)
            .anyMatch(p -> p.matches(target));
    }
}
