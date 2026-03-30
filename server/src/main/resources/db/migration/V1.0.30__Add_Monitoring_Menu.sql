-- 监控中心菜单模块

-- 监控中心目录
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('a1b2c3d4-e5f6-7890-1234-567890abc001', 'monitoring-root', '监控中心', 'DIRECTORY', '/monitoring', 50, NULL)
ON CONFLICT (code) DO NOTHING;

-- 监控概览菜单
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('a1b2c3d4-e5f6-7890-1234-567890abc002', 'monitoring-overview', '监控概览', 'MENU', '/monitoring', 1, (SELECT id FROM sys_module WHERE code='monitoring-root'))
ON CONFLICT (code) DO NOTHING;

-- Grafana 大盘菜单
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('a1b2c3d4-e5f6-7890-1234-567890abc003', 'grafana-dashboard', 'Grafana 大盘', 'MENU', '/monitoring/grafana', 2, (SELECT id FROM sys_module WHERE code='monitoring-root'))
ON CONFLICT (code) DO NOTHING;

-- Admin 角色获得监控中心菜单权限
INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code='monitoring-root'
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code='monitoring-overview'
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code='grafana-dashboard'
ON CONFLICT DO NOTHING;