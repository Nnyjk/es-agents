-- V202603292100__create_permissions.sql
-- RBAC 权限细化 - 创建权限表

-- 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    data_scope VARCHAR(20) NOT NULL DEFAULT 'ALL',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource);
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission ON role_permissions(permission_id);

-- 初始化基础权限数据
-- 插件管理权限
INSERT INTO permissions (id, code, name, resource, action, data_scope, description) VALUES
(gen_random_uuid(), 'plugin:read', '查看插件', 'plugin', 'read', 'ALL', '查看所有插件信息'),
(gen_random_uuid(), 'plugin:create', '创建插件', 'plugin', 'create', 'ALL', '创建新插件'),
(gen_random_uuid(), 'plugin:update', '更新插件', 'plugin', 'update', 'ALL', '更新插件信息'),
(gen_random_uuid(), 'plugin:delete', '删除插件', 'plugin', 'delete', 'ALL', '删除插件'),
(gen_random_uuid(), 'plugin:manage', '管理插件', 'plugin', 'manage', 'ALL', '插件全部权限');

-- 主机管理权限
INSERT INTO permissions (id, code, name, resource, action, data_scope, description) VALUES
(gen_random_uuid(), 'host:read', '查看主机', 'host', 'read', 'ALL', '查看所有主机信息'),
(gen_random_uuid(), 'host:create', '创建主机', 'host', 'create', 'ALL', '创建新主机'),
(gen_random_uuid(), 'host:update', '更新主机', 'host', 'update', 'ALL', '更新主机信息'),
(gen_random_uuid(), 'host:delete', '删除主机', 'host', 'delete', 'ALL', '删除主机'),
(gen_random_uuid(), 'host:manage', '管理主机', 'host', 'manage', 'ALL', '主机全部权限');

-- 部署管理权限
INSERT INTO permissions (id, code, name, resource, action, data_scope, description) VALUES
(gen_random_uuid(), 'deployment:read', '查看部署', 'deployment', 'read', 'ALL', '查看所有部署信息'),
(gen_random_uuid(), 'deployment:create', '创建部署', 'deployment', 'create', 'ALL', '创建新部署'),
(gen_random_uuid(), 'deployment:update', '更新部署', 'deployment', 'update', 'ALL', '更新部署信息'),
(gen_random_uuid(), 'deployment:delete', '删除部署', 'deployment', 'delete', 'ALL', '删除部署'),
(gen_random_uuid(), 'deployment:manage', '管理部署', 'deployment', 'manage', 'ALL', '部署全部权限');

-- 用户管理权限
INSERT INTO permissions (id, code, name, resource, action, data_scope, description) VALUES
(gen_random_uuid(), 'user:read', '查看用户', 'user', 'read', 'ALL', '查看所有用户信息'),
(gen_random_uuid(), 'user:create', '创建用户', 'user', 'create', 'ALL', '创建新用户'),
(gen_random_uuid(), 'user:update', '更新用户', 'user', 'update', 'ALL', '更新用户信息'),
(gen_random_uuid(), 'user:delete', '删除用户', 'user', 'delete', 'ALL', '删除用户'),
(gen_random_uuid(), 'user:manage', '管理用户', 'user', 'manage', 'ALL', '用户全部权限');

-- 角色管理权限
INSERT INTO permissions (id, code, name, resource, action, data_scope, description) VALUES
(gen_random_uuid(), 'role:read', '查看角色', 'role', 'read', 'ALL', '查看所有角色信息'),
(gen_random_uuid(), 'role:create', '创建角色', 'role', 'create', 'ALL', '创建新角色'),
(gen_random_uuid(), 'role:update', '更新角色', 'role', 'update', 'ALL', '更新角色信息'),
(gen_random_uuid(), 'role:delete', '删除角色', 'role', 'delete', 'ALL', '删除角色'),
(gen_random_uuid(), 'role:manage', '管理角色', 'role', 'manage', 'ALL', '角色全部权限');

-- 权限管理权限
INSERT INTO permissions (id, code, name, resource, action, data_scope, description) VALUES
(gen_random_uuid(), 'permission:read', '查看权限', 'permission', 'read', 'ALL', '查看所有权限信息'),
(gen_random_uuid(), 'permission:create', '创建权限', 'permission', 'create', 'ALL', '创建新权限'),
(gen_random_uuid(), 'permission:update', '更新权限', 'permission', 'update', 'ALL', '更新权限信息'),
(gen_random_uuid(), 'permission:delete', '删除权限', 'permission', 'delete', 'ALL', '删除权限'),
(gen_random_uuid(), 'permission:manage', '管理权限', 'permission', 'manage', 'ALL', '权限全部权限');

COMMENT ON TABLE permissions IS '权限表 - RBAC 细粒度权限控制';
COMMENT ON TABLE role_permissions IS '角色权限关联表';
COMMENT ON COLUMN permissions.code IS '权限编码，格式：resource:action';
COMMENT ON COLUMN permissions.resource IS '资源类型：plugin, host, deployment, user, role, permission';
COMMENT ON COLUMN permissions.action IS '操作类型：read, create, update, delete, manage';
COMMENT ON COLUMN permissions.data_scope IS '数据范围：ALL, DEPARTMENT, PROJECT, SELF';
