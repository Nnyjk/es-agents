-- ESA M1 验收演示数据准备脚本
-- 执行：psql -U postgres -d esa -f scripts/prepare-demo-data.sql

-- 1. 清理现有演示数据（可选）
-- DELETE FROM system_event_log WHERE created_by = 'demo';
-- DELETE FROM command_template WHERE created_by = 'demo';
-- DELETE FROM host WHERE created_by = 'demo';

-- 2. 插入测试主机数据
INSERT INTO host (name, ip, os_type, os_version, status, created_by, created_at, updated_at)
VALUES 
    ('demo-host-01', '192.168.1.101', 'linux', 'Ubuntu 22.04', 'ONLINE', 'demo', NOW(), NOW()),
    ('demo-host-02', '192.168.1.102', 'linux', 'CentOS 7.9', 'ONLINE', 'demo', NOW(), NOW()),
    ('demo-host-03', '192.168.1.103', 'linux', 'Debian 11', 'ONLINE', 'demo', NOW(), NOW()),
    ('demo-host-04', '192.168.1.104', 'linux', 'Ubuntu 20.04', 'OFFLINE', 'demo', NOW(), NOW()),
    ('demo-host-05', '192.168.1.105', 'windows', 'Windows Server 2019', 'ONLINE', 'demo', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET 
    ip = EXCLUDED.ip,
    os_type = EXCLUDED.os_type,
    os_version = EXCLUDED.os_version,
    status = EXCLUDED.status,
    updated_at = NOW();

-- 3. 插入命令模板数据
INSERT INTO command_template (name, description, content, timeout_seconds, category, created_by, created_at, updated_at)
VALUES 
    ('系统信息查询', '查看系统基本信息', 'uname -a && cat /etc/os-release', 30, 'system', 'demo', NOW(), NOW()),
    ('磁盘使用率', '查看磁盘使用情况', 'df -h', 30, 'system', 'demo', NOW(), NOW()),
    ('内存使用率', '查看内存使用情况', 'free -m', 30, 'system', 'demo', NOW(), NOW()),
    ('CPU 使用率', '查看 CPU 使用情况', 'top -bn1 | head -5', 30, 'system', 'demo', NOW(), NOW()),
    ('服务状态检查', '检查指定服务状态', 'systemctl status {{service_name}}', 60, 'service', 'demo', NOW(), NOW()),
    ('日志查看', '查看应用日志', 'tail -n 100 /var/log/{{log_file}}', 60, 'log', 'demo', NOW(), NOW()),
    ('进程列表', '查看运行中的进程', 'ps aux | head -20', 30, 'system', 'demo', NOW(), NOW()),
    ('网络接口', '查看网络接口信息', 'ip addr show', 30, 'network', 'demo', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    content = EXCLUDED.content,
    timeout_seconds = EXCLUDED.timeout_seconds,
    category = EXCLUDED.category,
    updated_at = NOW();

-- 4. 插入部署目标数据
INSERT INTO deployment_target (name, description, command_template_id, status, created_by, created_at, updated_at)
VALUES 
    ('日常巡检', '每日系统状态检查', (SELECT id FROM command_template WHERE name = '系统信息查询'), 'ACTIVE', 'demo', NOW(), NOW()),
    ('磁盘监控', '监控磁盘使用率', (SELECT id FROM command_template WHERE name = '磁盘使用率'), 'ACTIVE', 'demo', NOW(), NOW()),
    ('服务健康检查', '检查关键服务状态', (SELECT id FROM command_template WHERE name = '服务状态检查'), 'ACTIVE', 'demo', NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    command_template_id = EXCLUDED.command_template_id,
    status = EXCLUDED.status,
    updated_at = NOW();

-- 5. 插入系统事件日志数据
INSERT INTO system_event_log (event_type, severity, message, source, metadata, created_by, created_at)
VALUES 
    ('SYSTEM_STARTUP', 'INFO', 'ESA Server 启动成功', 'server', '{"version": "1.0.0", "port": 8080}', 'demo', NOW() - INTERVAL '1 hour'),
    ('AGENT_REGISTER', 'INFO', 'Agent 注册成功', 'server', '{"agent_id": "agent-001", "host": "demo-host-01"}', 'demo', NOW() - INTERVAL '55 minute'),
    ('AGENT_REGISTER', 'INFO', 'Agent 注册成功', 'server', '{"agent_id": "agent-002", "host": "demo-host-02"}', 'demo', NOW() - INTERVAL '50 minute'),
    ('AGENT_REGISTER', 'INFO', 'Agent 注册成功', 'server', '{"agent_id": "agent-003", "host": "demo-host-03"}', 'demo', NOW() - INTERVAL '45 minute'),
    ('BATCH_TASK_CREATED', 'INFO', '批量任务创建成功', 'server', '{"task_id": 1, "host_count": 3}', 'demo', NOW() - INTERVAL '40 minute'),
    ('BATCH_TASK_STARTED', 'INFO', '批量任务开始执行', 'server', '{"task_id": 1}', 'demo', NOW() - INTERVAL '39 minute'),
    ('COMMAND_EXECUTED', 'INFO', '命令执行成功', 'agent', '{"host": "demo-host-01", "command": "uname -a"}', 'demo', NOW() - INTERVAL '38 minute'),
    ('COMMAND_EXECUTED', 'INFO', '命令执行成功', 'agent', '{"host": "demo-host-02", "command": "uname -a"}', 'demo', NOW() - INTERVAL '37 minute'),
    ('COMMAND_EXECUTED', 'INFO', '命令执行成功', 'agent', '{"host": "demo-host-03", "command": "uname -a"}', 'demo', NOW() - INTERVAL '36 minute'),
    ('BATCH_TASK_COMPLETED', 'INFO', '批量任务执行完成', 'server', '{"task_id": 1, "success": 3, "failed": 0}', 'demo', NOW() - INTERVAL '35 minute'),
    ('AGENT_UPGRADE_STARTED', 'INFO', 'Agent 升级开始', 'server', '{"agent_id": "agent-001", "target_version": "1.0.1"}', 'demo', NOW() - INTERVAL '30 minute'),
    ('AGENT_UPGRADE_COMPLETED', 'INFO', 'Agent 升级成功', 'agent', '{"agent_id": "agent-001", "version": "1.0.1"}', 'demo', NOW() - INTERVAL '25 minute'),
    ('CONFIG_CHANGED', 'WARN', '配置变更', 'server', '{"config_key": "max_concurrent_tasks", "old_value": 5, "new_value": 10}', 'demo', NOW() - INTERVAL '20 minute'),
    ('AUTH_FAILURE', 'ERROR', '认证失败', 'server', '{"username": "test", "reason": "invalid_password"}', 'demo', NOW() - INTERVAL '15 minute'),
    ('SYSTEM_SHUTDOWN', 'INFO', 'ESA Server 正常关闭', 'server', '{"version": "1.0.0", "uptime": "24h"}', 'demo', NOW() - INTERVAL '10 minute')
ON CONFLICT DO NOTHING;

-- 6. 查询验证
SELECT 'Hosts' as table_name, COUNT(*) as count FROM host WHERE created_by = 'demo'
UNION ALL
SELECT 'Command Templates', COUNT(*) FROM command_template WHERE created_by = 'demo'
UNION ALL
SELECT 'Deployment Targets', COUNT(*) FROM deployment_target WHERE created_by = 'demo'
UNION ALL
SELECT 'System Event Logs', COUNT(*) FROM system_event_log WHERE created_by = 'demo';
