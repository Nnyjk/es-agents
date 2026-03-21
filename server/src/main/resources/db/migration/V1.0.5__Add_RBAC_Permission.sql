-- 权限表
CREATE TABLE sys_permission (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    resource VARCHAR(100),
    action VARCHAR(100),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

-- 索引
CREATE INDEX idx_permission_code ON sys_permission(code);
CREATE INDEX idx_permission_resource ON sys_permission(resource);
CREATE INDEX idx_permission_action ON sys_permission(action);
CREATE INDEX idx_role_permission_role_id ON sys_role_permission(role_id);
CREATE INDEX idx_role_permission_permission_id ON sys_role_permission(permission_id);

-- 初始化默认权限
INSERT INTO sys_permission (id, code, name, description, resource, action, is_system, created_at, updated_at) VALUES
    (gen_random_uuid(), 'agent:view', '查看Agent', '查看Agent列表和详情', 'agent', 'view', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'agent:create', '创建Agent', '创建新的Agent', 'agent', 'create', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'agent:edit', '编辑Agent', '编辑Agent配置', 'agent', 'edit', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'agent:delete', '删除Agent', '删除Agent', 'agent', 'delete', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'agent:execute', '执行Agent命令', '执行Agent命令操作', 'agent', 'execute', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'host:view', '查看主机', '查看主机列表和详情', 'host', 'view', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'host:create', '创建主机', '添加新主机', 'host', 'create', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'host:edit', '编辑主机', '编辑主机信息', 'host', 'edit', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'host:delete', '删除主机', '删除主机', 'host', 'delete', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'deployment:view', '查看部署', '查看部署记录', 'deployment', 'view', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'deployment:execute', '执行部署', '执行部署操作', 'deployment', 'execute', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'deployment:rollback', '回滚部署', '回滚部署', 'deployment', 'rollback', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'user:view', '查看用户', '查看用户列表', 'user', 'view', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'user:create', '创建用户', '创建新用户', 'user', 'create', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'user:edit', '编辑用户', '编辑用户信息', 'user', 'edit', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'user:delete', '删除用户', '删除用户', 'user', 'delete', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'role:view', '查看角色', '查看角色列表', 'role', 'view', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'role:manage', '管理角色', '管理角色和权限', 'role', 'manage', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'system:settings', '系统设置', '系统全局设置', 'system', 'settings', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'system:audit', '审计日志', '查看操作审计日志', 'system', 'audit', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);