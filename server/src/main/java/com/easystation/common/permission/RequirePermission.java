package com.easystation.common.permission;

import com.easystation.auth.model.Permission.DataScope;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限验证注解
 * 
 * 用于标记需要权限验证的方法或类
 * 
 * 使用示例：
 * ```java
 * @RequirePermission(resource = "plugin", action = "create")
 * public Response createPlugin(PluginDTO dto) { ... }
 * 
 * @RequirePermission(resource = "host", action = "delete", dataScope = DataScope.DEPARTMENT)
 * public Response deleteHost(UUID id) { ... }
 * ```
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 资源类型
     * 例如：plugin, host, deployment, user, role
     */
    String resource();

    /**
     * 操作类型
     * 例如：read, create, update, delete, manage
     */
    String action();

    /**
     * 数据范围
     * 默认：ALL（全部数据）
     */
    DataScope dataScope() default DataScope.ALL;

    /**
     * 权限描述（可选）
     */
    String description() default "";
}
