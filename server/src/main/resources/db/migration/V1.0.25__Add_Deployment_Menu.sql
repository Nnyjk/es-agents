-- 部署管理菜单模块

-- 部署管理菜单 - 放在 基础设施 目录下
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('d3e4f5a6-b7c8-9012-3456-789012cdef01', 'deployment-mgmt', '部署管理', 'MENU', '/infra/deployments', 3, (SELECT id FROM sys_module WHERE code='infra-root'))
ON CONFLICT (code) DO NOTHING;

-- Admin 角色获得部署管理菜单权限
INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code='deployment-mgmt'
ON CONFLICT DO NOTHING;