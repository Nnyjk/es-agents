package com.easystation.common.permission;

import com.easystation.auth.model.Permission;
import com.easystation.auth.model.RolePermission;
import com.easystation.auth.service.PermissionService;
import com.easystation.common.exception.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

/**
 * 权限验证 CDI 拦截器
 * 
 * 拦截标记了 @RequirePermission 注解的方法，进行权限验证
 */
@Interceptor
@RequirePermission(resource = "", action = "")
public class PermissionInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionInterceptor.class);

    @Inject
    PermissionService permissionService;

    @Inject
    SecurityIdentity identity;

    @Inject
    JsonWebToken jwt;

    /**
     * 权限验证拦截方法
     */
    @AroundInvoke
    public Object verifyPermission(InvocationContext context) throws Exception {
        // 获取方法或类上的注解
        RequirePermission requirePermission = getRequirePermission(context);
        
        if (requirePermission == null) {
            // 没有注解，直接执行
            return context.proceed();
        }

        // 获取当前用户 ID
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            LOG.warn("未认证用户尝试访问受保护资源：{}", context.getMethod().getName());
            throw new UnauthorizedException("未认证用户");
        }

        // 验证权限
        String resource = requirePermission.resource();
        String action = requirePermission.action();
        Permission.DataScope dataScope = requirePermission.dataScope();

        LOG.debug("验证权限 - 用户：{}, 资源：{}, 操作：{}, 数据范围：{}", 
                  userId, resource, action, dataScope);

        boolean hasPermission = permissionService.hasPermission(
            UUID.fromString(userId), 
            resource, 
            action
        );

        if (!hasPermission) {
            LOG.warn("用户 {} 缺少权限：{}:{}", userId, resource, action);
            throw new UnauthorizedException(String.format(
                "缺少权限：%s:%s", resource, action
            ));
        }

        // 验证数据范围（如果需要）
        if (dataScope != Permission.DataScope.ALL) {
            // 数据范围验证在业务层处理，这里只记录
            LOG.debug("数据范围限制：{}", dataScope);
        }

        return context.proceed();
    }

    /**
     * 获取方法或类上的 RequirePermission 注解
     */
    private RequirePermission getRequirePermission(InvocationContext context) {
        // 先检查方法上的注解
        RequirePermission methodAnnotation = context.getMethod()
            .getAnnotation(RequirePermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 再检查类上的注解
        RequirePermission classAnnotation = context.getTarget().getClass()
            .getAnnotation(RequirePermission.class);
        return classAnnotation;
    }

    /**
     * 获取当前用户 ID
     */
    private String getCurrentUserId() {
        // 从 JWT token 中获取用户 ID
        if (jwt != null) {
            return jwt.getSubject();
        }
        // 从 SecurityIdentity 获取
        if (identity != null && !identity.isAnonymous()) {
            return identity.getPrincipal().getName();
        }
        return null;
    }
}
