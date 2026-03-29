package com.easystation.auth.service;

import com.easystation.auth.model.Permission;
import com.easystation.auth.model.RolePermission;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限验证服务
 * 
 * 提供权限查询、验证功能，支持 Redis 缓存
 */
@ApplicationScoped
public class PermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionService.class);

    /** 权限缓存过期时间：5 分钟 */
    private static final Duration PERMISSION_CACHE_TTL = Duration.ofMinutes(5);

    @Inject
    RedisDataSource redis;

    /**
     * 检查用户是否有指定权限
     * 
     * @param userId 用户 ID
     * @param resource 资源类型
     * @param action 操作类型
     * @return 是否有权限
     */
    public boolean hasPermission(UUID userId, String resource, String action) {
        String permissionCode = resource + ":" + action;
        String manageCode = resource + ":manage";
        
        // 从缓存获取用户权限
        Set<String> userPermissions = getUserPermissionsFromCache(userId);
        
        // 检查是否有指定权限或 manage 权限
        return userPermissions.contains(permissionCode) || 
               userPermissions.contains(manageCode);
    }

    /**
     * 获取用户所有权限编码
     * 
     * @param userId 用户 ID
     * @return 权限编码集合
     */
    public Set<String> getUserPermissions(UUID userId) {
        return getUserPermissionsFromCache(userId);
    }

    /**
     * 从缓存获取用户权限（缓存未命中时从数据库加载）
     */
    private Set<String> getUserPermissionsFromCache(UUID userId) {
        String cacheKey = "user:permissions:" + userId;
        
        // 尝试从 Redis 缓存获取
        Set<String> cached = redis.value().get(cacheKey);
        if (cached != null) {
            LOG.debug("权限缓存命中：{}", userId);
            return cached;
        }

        // 缓存未命中，从数据库加载
        LOG.debug("权限缓存未命中，从数据库加载：{}", userId);
        Set<String> permissions = loadPermissionsFromDatabase(userId);
        
        // 写入缓存
        redis.value().setex(cacheKey, PERMISSION_CACHE_TTL, permissions);
        
        return permissions;
    }

    /**
     * 从数据库加载用户权限
     */
    private Set<String> loadPermissionsFromDatabase(UUID userId) {
        // 获取用户所有角色
        List<UUID> roleIds = getUserRoleIds(userId);
        
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // 获取角色所有权限
        List<RolePermission> rolePermissions = RolePermission.list(
            "roleId in ?1", roleIds
        );

        if (rolePermissions.isEmpty()) {
            return Collections.emptySet();
        }

        // 获取权限详情
        List<UUID> permissionIds = rolePermissions.stream()
            .map(rp -> rp.permissionId)
            .collect(Collectors.toList());

        List<Permission> permissions = Permission.list("id in ?1", permissionIds);

        // 转换为权限编码集合
        return permissions.stream()
            .map(p -> p.code)
            .collect(Collectors.toSet());
    }

    /**
     * 获取用户角色 ID 列表
     */
    private List<UUID> getUserRoleIds(UUID userId) {
        // TODO: 实现用户角色查询
        // 这里假设有一个 user_roles 表
        return new ArrayList<>();
    }

    /**
     * 清除用户权限缓存（权限变更时调用）
     */
    public void clearUserPermissionCache(UUID userId) {
        String cacheKey = "user:permissions:" + userId;
        redis.value().del(cacheKey);
        LOG.debug("清除用户权限缓存：{}", userId);
    }

    /**
     * 创建权限
     */
    @Transactional
    public Permission createPermission(String code, String name, String resource, 
                                        String action, Permission.DataScope dataScope,
                                        String description) {
        Permission permission = new Permission();
        permission.code = code;
        permission.name = name;
        permission.resource = resource;
        permission.action = action;
        permission.dataScope = dataScope;
        permission.description = description;
        permission.persist();
        
        LOG.info("创建权限：{} ({})", code, name);
        return permission;
    }

    /**
     * 删除权限
     */
    @Transactional
    public boolean deletePermission(UUID id) {
        Permission permission = Permission.findById(id);
        if (permission == null) {
            return false;
        }
        
        // 删除角色权限关联
        RolePermission.delete("permissionId", id);
        
        // 删除权限
        permission.delete();
        
        LOG.info("删除权限：{}", permission.code);
        return true;
    }

    /**
     * 获取所有权限
     */
    public List<Permission> getAllPermissions() {
        return Permission.listAll();
    }

    /**
     * 根据资源类型获取权限
     */
    public List<Permission> getPermissionsByResource(String resource) {
        return Permission.list("resource", resource);
    }
}
