-- 批量操作菜单模块

-- 批量操作菜单 - 放在 Agent管理 目录下
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('b1a2c3d4-e5f6-7890-1234-567890abcdef', 'batch-operation', '批量操作', 'MENU', '/batch', 5, (SELECT id FROM sys_module WHERE code='agent-root'))
ON CONFLICT (code) DO NOTHING;

-- Admin 角色获得批量操作菜单权限
INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code='batch-operation'
ON CONFLICT DO NOTHING;