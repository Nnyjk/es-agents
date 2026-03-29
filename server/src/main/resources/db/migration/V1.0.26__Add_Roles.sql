-- 添加运维和只读角色
-- V1.0.26__Add_Roles.sql

-- 创建 operator 角色（运维人员）
INSERT INTO sys_role (id, code, name, description) VALUES 
    (gen_random_uuid(), 'operator', 'Operator', '运维人员 - 可执行日常运维操作，不能创建/删除资源')
ON CONFLICT (code) DO NOTHING;

-- 创建 viewer 角色（只读用户）
INSERT INTO sys_role (id, code, name, description) VALUES 
    (gen_random_uuid(), 'viewer', 'Viewer', '只读用户 - 仅查看权限')
ON CONFLICT (code) DO NOTHING;

-- 为 operator 角色分配权限（view + edit + execute，不含 create/delete）
INSERT INTO sys_role_permission (id, role_id, permission_id, created_at)
SELECT gen_random_uuid(), r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r, sys_permission p
WHERE r.code = 'operator' 
  AND p.code IN (
    -- agent 权限
    'agent:view', 'agent:edit', 'agent:execute',
    -- host 权限
    'host:view', 'host:edit', 'host:execute',
    -- deployment 权限
    'deployment:view', 'deployment:execute', 'deployment:rollback',
    -- environment 权限（如果有）
    'environment:view', 'environment:edit',
    -- 系统和权限查看
    'permission:view', 'module:view', 'audit:view',
    -- 用户查看（不含创建/编辑/删除）
    'user:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为 viewer 角色分配权限（仅 view）
INSERT INTO sys_role_permission (id, role_id, permission_id, created_at)
SELECT gen_random_uuid(), r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r, sys_permission p
WHERE r.code = 'viewer' 
  AND p.code IN (
    -- 所有 view 权限
    'agent:view',
    'host:view',
    'deployment:view',
    'environment:view',
    'user:view',
    'role:view',
    'permission:view',
    'module:view',
    'audit:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;
