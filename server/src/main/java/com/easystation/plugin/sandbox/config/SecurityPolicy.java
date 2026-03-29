package com.easystation.plugin.sandbox.config;

import com.easystation.plugin.sandbox.domain.Permission;
import com.easystation.plugin.sandbox.domain.PermissionType;
import com.easystation.plugin.sandbox.domain.ResourceLimit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全策略
 */
@ApplicationScoped
public class SecurityPolicy {
    
    private static final Logger LOG = Logger.getLogger(SecurityPolicy.class);
    
    @Inject
    SandboxConfig config;
    
    public enum TrustLevel {
        TRUSTED,
        NORMAL,
        UNTRUSTED
    }
    
    public ResourceLimit getDefaultLimit() {
        return config.limits().toResourceLimit();
    }
    
    public ResourceLimit getLimitForTrustLevel(TrustLevel level) {
        return switch (level) {
            case TRUSTED -> ResourceLimit.relaxed();
            case UNTRUSTED -> ResourceLimit.strict();
            default -> getDefaultLimit();
        };
    }
    
    public List<Permission> getDefaultPermissions() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.fileRead("/plugins/data/*"));
        permissions.add(Permission.fileWrite("/plugins/data/*"));
        permissions.add(Permission.networkAccess("localhost"));
        permissions.add(Permission.networkAccess("127.0.0.1"));
        return permissions;
    }
}
