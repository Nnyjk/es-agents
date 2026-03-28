-- 通知管理菜单模块

-- Settings 目录（如果不存在）
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('d1a2b3c4-5678-90ab-cdef-123456789012', 'settings-root', '设置管理', 'DIRECTORY', '/settings', 250, NULL)
ON CONFLICT (code) DO NOTHING;

-- API密钥管理
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('e1f2a3b4-5678-90cd-ef12-34567890abcd', 'api-key-mgmt', 'API密钥', 'MENU', '/settings/api-keys', 1, (SELECT id FROM sys_module WHERE code='settings-root'))
ON CONFLICT (code) DO NOTHING;

-- 通知渠道管理
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('f1a2b3c4-5678-90de-f123-45678901bcde', 'notification-channels-mgmt', '通知渠道', 'MENU', '/settings/notification-channels', 2, (SELECT id FROM sys_module WHERE code='settings-root'))
ON CONFLICT (code) DO NOTHING;

-- 通知模板管理
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('a1b2c3d4-5678-90ef-1234-56789012cdef', 'notification-templates-mgmt', '通知模板', 'MENU', '/settings/notification-templates', 3, (SELECT id FROM sys_module WHERE code='settings-root'))
ON CONFLICT (code) DO NOTHING;

-- 通知历史管理
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('b1c2d3e4-5678-9012-3456-78901234def0', 'notification-history-mgmt', '通知历史', 'MENU', '/settings/notification-history', 4, (SELECT id FROM sys_module WHERE code='settings-root'))
ON CONFLICT (code) DO NOTHING;

-- Admin 角色获得所有新模块权限
INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m
WHERE r.code='admin' AND m.code IN ('settings-root', 'api-key-mgmt', 'notification-channels-mgmt', 'notification-templates-mgmt', 'notification-history-mgmt')
ON CONFLICT DO NOTHING;