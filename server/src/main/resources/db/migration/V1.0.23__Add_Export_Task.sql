-- V1.0.23 Add Export Task Table

-- Export Task table for tracking data export operations
CREATE TABLE IF NOT EXISTS export_task (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    export_type VARCHAR(20) NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    total_records INTEGER,
    error_message TEXT,
    created_at TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    query_params TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_export_task_user_id ON export_task(user_id);
CREATE INDEX IF NOT EXISTS idx_export_task_status ON export_task(status);
CREATE INDEX IF NOT EXISTS idx_export_task_created_at ON export_task(created_at);

-- Add export menu
INSERT INTO sys_menu (id, name, path, component, permission, parent_id, sort, icon, type, visible, created_at, updated_at)
SELECT
    'export_menu_id'::uuid,
    '数据导出',
    '/export',
    'export/index',
    'export:view',
    NULL,
    100,
    'download',
    'MENU',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/export');

-- Add export permissions
INSERT INTO sys_permission (id, name, code, description, type, created_at, updated_at)
SELECT gen_random_uuid(), '导出数据', 'export:create', '创建数据导出任务', 'OPERATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'export:create');

INSERT INTO sys_permission (id, name, code, description, type, created_at, updated_at)
SELECT gen_random_uuid(), '下载导出文件', 'export:download', '下载导出的数据文件', 'OPERATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'export:download');

INSERT INTO sys_permission (id, name, code, description, type, created_at, updated_at)
SELECT gen_random_uuid(), '查看导出任务', 'export:view', '查看数据导出任务列表', 'OPERATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'export:view');