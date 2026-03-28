-- 系统事件日志菜单模块

-- 系统事件日志菜单 - 放在 系统管理 目录下
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('c2d3e4f5-a6b7-8901-2345-678901bcdefg', 'system-event-log', '系统事件日志', 'MENU', '/system-event-log', 4, (SELECT id FROM sys_module WHERE code='system-root'))
ON CONFLICT (code) DO NOTHING;

-- Admin 角色获得系统事件日志菜单权限
INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code='system-event-log'
ON CONFLICT DO NOTHING;
