package com.easystation.auth.interceptor;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.auth.service.PermissionService;
import com.easystation.system.domain.User;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Set;

/**
 * 权限拦截器
 * 检查用户是否有执行操作所需的权限
 */
@Interceptor
@RequiresPermission("")
@Priority(Interceptor.Priority.APPLICATION)
public class PermissionInterceptor {

    @Inject
    SecurityContext securityContext;

    @Inject
    PermissionService permissionService;

    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        // 获取注解
        RequiresPermission annotation = context.getMethod().getAnnotation(RequiresPermission.class);
        if (annotation == null) {
            annotation = context.getTarget().getClass().getAnnotation(RequiresPermission.class);
        }

        // 如果没有注解或权限码为空，直接放行
        if (annotation == null || annotation.value().isEmpty()) {
            return context.proceed();
        }

        String permissionCode = annotation.value();
        
        // 检查是否已认证
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            Log.warnf("Permission check failed: not authenticated for permission %s", permissionCode);
            throw new WebApplicationException("未认证", Response.Status.UNAUTHORIZED);
        }

        String username = securityContext.getUserPrincipal().getName();
        
        // 查询用户
        User user = User.find("username", username).firstResult();
        if (user == null) {
            Log.warnf("Permission check failed: user not found %s", username);
            throw new WebApplicationException("用户不存在", Response.Status.UNAUTHORIZED);
        }

        // 获取用户所有权限码（包含管理员判断）
        Set<String> userPermissions = permissionService.getUserPermissions(user.id);
        
        if (userPermissions.contains(permissionCode)) {
            Log.debugf("Permission granted: %s for user %s", permissionCode, username);
            return context.proceed();
        }

        Log.warnf("Permission denied: %s for user %s (available: %s)", permissionCode, username, userPermissions);
        throw new WebApplicationException("权限不足: " + permissionCode, Response.Status.FORBIDDEN);
    }
}