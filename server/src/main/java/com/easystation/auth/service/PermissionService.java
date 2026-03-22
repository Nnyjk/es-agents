package com.easystation.auth.service;

import com.easystation.auth.domain.Permission;
import com.easystation.auth.domain.RolePermission;
import com.easystation.auth.dto.PermissionRecord;
import com.easystation.system.domain.Role;
import com.easystation.system.domain.User;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PermissionService {

    public List<PermissionRecord.Detail> list(PermissionRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and (code like :keyword or name like :keyword)");
            params.put("keyword", "%" + query.keyword() + "%");
        }
        if (query.resource() != null && !query.resource().isBlank()) {
            sql.append(" and resource = :resource");
            params.put("resource", query.resource());
        }
        if (query.action() != null && !query.action().isBlank()) {
            sql.append(" and action = :action");
            params.put("action", query.action());
        }

        int limit = query.limit() != null ? query.limit() : 100;
        int offset = query.offset() != null ? query.offset() : 0;

        return Permission.<Permission>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public PermissionRecord.Detail get(UUID id) {
        Permission permission = Permission.findById(id);
        if (permission == null) {
            throw new WebApplicationException("Permission not found", Response.Status.NOT_FOUND);
        }
        return toDetail(permission);
    }

    @Transactional
    public PermissionRecord.Detail create(PermissionRecord.Create dto) {
        if (Permission.find("code", dto.code()).firstResult() != null) {
            throw new WebApplicationException("Permission code already exists", Response.Status.CONFLICT);
        }

        Permission permission = new Permission();
        permission.code = dto.code();
        permission.name = dto.name();
        permission.description = dto.description();
        permission.resource = dto.resource();
        permission.action = dto.action();
        permission.system = dto.system() != null ? dto.system() : false;
        permission.persist();

        Log.infof("Permission created: %s", permission.code);
        return toDetail(permission);
    }

    @Transactional
    public PermissionRecord.Detail update(UUID id, PermissionRecord.Update dto) {
        Permission permission = Permission.findById(id);
        if (permission == null) {
            throw new WebApplicationException("Permission not found", Response.Status.NOT_FOUND);
        }

        if (permission.system) {
            throw new WebApplicationException("System permission cannot be modified", Response.Status.FORBIDDEN);
        }

        if (dto.name() != null) permission.name = dto.name();
        if (dto.description() != null) permission.description = dto.description();
        if (dto.resource() != null) permission.resource = dto.resource();
        if (dto.action() != null) permission.action = dto.action();

        return toDetail(permission);
    }

    @Transactional
    public void delete(UUID id) {
        Permission permission = Permission.findById(id);
        if (permission == null) {
            throw new WebApplicationException("Permission not found", Response.Status.NOT_FOUND);
        }

        if (permission.system) {
            throw new WebApplicationException("System permission cannot be deleted", Response.Status.FORBIDDEN);
        }

        // Remove role associations
        RolePermission.delete("permissionId", id);
        permission.delete();

        Log.infof("Permission deleted: %s", permission.code);
    }

    @Transactional
    public void assignToRole(PermissionRecord.AssignPermissions dto) {
        Role role = Role.findById(dto.roleId());
        if (role == null) {
            throw new WebApplicationException("Role not found", Response.Status.NOT_FOUND);
        }

        // Remove existing permissions for this role
        RolePermission.delete("roleId", dto.roleId());

        // Add new permissions
        for (UUID permissionId : dto.permissionIds()) {
            Permission permission = Permission.findById(permissionId);
            if (permission != null) {
                RolePermission rp = new RolePermission();
                rp.roleId = dto.roleId();
                rp.permissionId = permissionId;
                rp.persist();
            }
        }

        Log.infof("Assigned %d permissions to role: %s", dto.permissionIds().size(), role.name);
    }

    public List<UUID> getRolePermissions(UUID roleId) {
        return RolePermission.<RolePermission>find("roleId", roleId)
                .stream()
                .map(rp -> rp.permissionId)
                .collect(Collectors.toList());
    }

    public PermissionRecord.CheckResult checkPermission(PermissionRecord.CheckRequest dto) {
        User user = User.findById(dto.userId());
        if (user == null) {
            return new PermissionRecord.CheckResult(false, dto.permissionCode(), "User not found");
        }

        Permission permission = Permission.find("code", dto.permissionCode()).firstResult();
        if (permission == null) {
            return new PermissionRecord.CheckResult(false, dto.permissionCode(), "Permission not found");
        }

        // Get all role IDs for the user
        Set<UUID> roleIds = user.roles != null 
                ? user.roles.stream().map(r -> r.id).collect(Collectors.toSet())
                : Collections.emptySet();

        if (roleIds.isEmpty()) {
            return new PermissionRecord.CheckResult(false, dto.permissionCode(), "User has no roles");
        }

        // Check if any role has the permission
        boolean hasPermission = RolePermission.count("roleId in ?1 and permissionId = ?2", 
                roleIds, permission.id) > 0;

        return new PermissionRecord.CheckResult(
                hasPermission, 
                dto.permissionCode(), 
                hasPermission ? "Permission granted" : "Permission denied"
        );
    }

    public PermissionRecord.UserPermissions getUserPermissions(UUID userId) {
        User user = User.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
        }

        // Get role codes
        List<String> roles = user.roles != null
                ? user.roles.stream().map(r -> r.code).collect(Collectors.toList())
                : Collections.emptyList();

        // Get all role IDs
        Set<UUID> roleIds = user.roles != null
                ? user.roles.stream().map(r -> r.id).collect(Collectors.toSet())
                : Collections.emptySet();

        // Get all permission IDs from roles
        Set<UUID> permissionIds = new HashSet<>();
        if (!roleIds.isEmpty()) {
            List<RolePermission> rolePermissions = RolePermission.<RolePermission>find("roleId in ?1", roleIds).list();
            permissionIds = rolePermissions.stream()
                    .map(rp -> rp.permissionId)
                    .collect(Collectors.toSet());
        }

        // Get permission codes
        List<String> permissions = new ArrayList<>();
        if (!permissionIds.isEmpty()) {
            permissions = Permission.<Permission>find("id in ?1", permissionIds)
                    .stream()
                    .map(p -> p.code)
                    .collect(Collectors.toList());
        }

        return new PermissionRecord.UserPermissions(
                userId,
                user.username,
                roles,
                permissions
        );
    }

    /**
     * 获取用户的所有权限码
     */
    public Set<String> getUserPermissionCodes(UUID userId) {
        User user = User.findById(userId);
        if (user == null) {
            return Collections.emptySet();
        }

        Set<String> permissionCodes = new HashSet<>();

        // 检查是否是管理员
        boolean isAdmin = user.roles != null && 
                user.roles.stream().anyMatch(r -> "admin".equals(r.code));
        
        if (isAdmin) {
            // 管理员拥有所有权限
            return Permission.<Permission>listAll().stream()
                    .map(p -> p.code)
                    .collect(Collectors.toSet());
        }

        // 获取用户角色的权限
        if (user.roles != null) {
            Set<UUID> roleIds = user.roles.stream()
                    .map(r -> r.id)
                    .collect(Collectors.toSet());

            if (!roleIds.isEmpty()) {
                List<RolePermission> rolePermissions = RolePermission.<RolePermission>find("roleId in ?1", roleIds).list();
                Set<UUID> permissionIds = rolePermissions.stream()
                        .map(rp -> rp.permissionId)
                        .collect(Collectors.toSet());

                if (!permissionIds.isEmpty()) {
                    permissionCodes = Permission.<Permission>find("id in ?1", permissionIds)
                            .stream()
                            .map(p -> p.code)
                            .collect(Collectors.toSet());
                }
            }
        }

        return permissionCodes;
    }

    @Transactional
    public void initDefaultPermissions() {
        // Create default permissions for common resources
        String[][] defaultPermissions = {
                {"agent:view", "查看Agent", "agent", "view", "查看Agent列表和详情"},
                {"agent:create", "创建Agent", "agent", "create", "创建新的Agent"},
                {"agent:edit", "编辑Agent", "agent", "edit", "编辑Agent配置"},
                {"agent:delete", "删除Agent", "agent", "delete", "删除Agent"},
                {"agent:execute", "执行Agent命令", "agent", "execute", "执行Agent命令操作"},
                {"host:view", "查看主机", "host", "view", "查看主机列表和详情"},
                {"host:create", "创建主机", "host", "create", "添加新主机"},
                {"host:edit", "编辑主机", "host", "edit", "编辑主机信息"},
                {"host:delete", "删除主机", "host", "delete", "删除主机"},
                {"deployment:view", "查看部署", "deployment", "view", "查看部署记录"},
                {"deployment:execute", "执行部署", "deployment", "execute", "执行部署操作"},
                {"deployment:rollback", "回滚部署", "deployment", "rollback", "回滚部署"},
                {"user:view", "查看用户", "user", "view", "查看用户列表"},
                {"user:create", "创建用户", "user", "create", "创建新用户"},
                {"user:edit", "编辑用户", "user", "edit", "编辑用户信息"},
                {"user:delete", "删除用户", "user", "delete", "删除用户"},
                {"role:view", "查看角色", "role", "view", "查看角色列表"},
                {"role:manage", "管理角色", "role", "manage", "管理角色和权限"},
                {"system:settings", "系统设置", "system", "settings", "系统全局设置"},
                {"system:audit", "审计日志", "system", "audit", "查看操作审计日志"},
        };

        for (String[] p : defaultPermissions) {
            if (Permission.find("code", p[0]).firstResult() == null) {
                Permission permission = new Permission();
                permission.code = p[0];
                permission.name = p[1];
                permission.resource = p[2];
                permission.action = p[3];
                permission.description = p[4];
                permission.system = true;
                permission.persist();
            }
        }

        Log.info("Default permissions initialized");
    }

    private PermissionRecord.Detail toDetail(Permission permission) {
        return new PermissionRecord.Detail(
                permission.id,
                permission.code,
                permission.name,
                permission.description,
                permission.resource,
                permission.action,
                permission.system,
                permission.createdAt,
                permission.updatedAt
        );
    }
}