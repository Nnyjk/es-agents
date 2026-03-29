package com.easystation.plugin.sandbox.config;

import com.easystation.plugin.sandbox.domain.ResourceLimit;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;

/**
 * 沙箱配置
 */
@ConfigMapping(prefix = "plugin.sandbox")
@ConfigRoot
public interface SandboxConfig {
    
    @WithDefault("true")
    boolean enabled();
    
    ResourceLimitConfig limits();
    
    List<PermissionConfig> permissions();
    
    interface ResourceLimitConfig {
        @WithDefault("256")
        int maxMemoryMb();
        
        @WithDefault("5")
        int maxCpuTimeSeconds();
        
        @WithDefault("30")
        int maxExecutionTimeSeconds();
        
        @WithDefault("100")
        int maxFileOperations();
        
        @WithDefault("50")
        int maxNetworkCalls();
        
        default ResourceLimit toResourceLimit() {
            return new ResourceLimit(
                maxMemoryMb() * 1024 * 1024L,
                maxCpuTimeSeconds(),
                maxExecutionTimeSeconds(),
                maxFileOperations(),
                maxNetworkCalls()
            );
        }
    }
    
    interface PermissionConfig {
        String type();
        String scope();
        @WithDefault("true")
        boolean enabled();
    }
}
