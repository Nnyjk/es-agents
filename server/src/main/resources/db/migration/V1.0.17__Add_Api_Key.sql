-- API Key 表
CREATE TABLE api_keys (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    secret VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    permissions TEXT,
    ip_whitelist TEXT,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    revoked_by UUID,
    revoke_reason VARCHAR(500)
);

-- 索引
CREATE INDEX idx_api_keys_name ON api_keys(name);
CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);
CREATE INDEX idx_api_keys_enabled ON api_keys(enabled);
CREATE INDEX idx_api_keys_expires_at ON api_keys(expires_at);
CREATE INDEX idx_api_keys_created_by ON api_keys(created_by);

-- API Key 使用日志表
CREATE TABLE api_key_usage_logs (
    id UUID PRIMARY KEY,
    key_id UUID NOT NULL REFERENCES api_keys(id) ON DELETE CASCADE,
    usage_time TIMESTAMP NOT NULL,
    client_ip VARCHAR(50),
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    response_status INTEGER,
    response_time_ms BIGINT,
    permission_used VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_api_key_usage_key_id ON api_key_usage_logs(key_id);
CREATE INDEX idx_api_key_usage_time ON api_key_usage_logs(usage_time);
CREATE INDEX idx_api_key_usage_client_ip ON api_key_usage_logs(client_ip);

-- 插入默认权限
INSERT INTO sys_permission (id, code, name, description, category, created_at)
SELECT gen_random_uuid(), 'api-key:view', '查看API密钥', '查看API密钥列表和详情', 'auth', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'api-key:view');

INSERT INTO sys_permission (id, code, name, description, category, created_at)
SELECT gen_random_uuid(), 'api-key:create', '创建API密钥', '创建新的API密钥', 'auth', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'api-key:create');

INSERT INTO sys_permission (id, code, name, description, category, created_at)
SELECT gen_random_uuid(), 'api-key:edit', '编辑API密钥', '编辑API密钥信息', 'auth', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'api-key:edit');

INSERT INTO sys_permission (id, code, name, description, category, created_at)
SELECT gen_random_uuid(), 'api-key:delete', '删除API密钥', '删除API密钥', 'auth', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'api-key:delete');

INSERT INTO sys_permission (id, code, name, description, category, created_at)
SELECT gen_random_uuid(), 'api-key:revoke', '吊销API密钥', '吊销API密钥', 'auth', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'api-key:revoke');

INSERT INTO sys_permission (id, code, name, description, category, created_at)
SELECT gen_random_uuid(), 'api-key:refresh', '刷新API密钥', '刷新API密钥', 'auth', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'api-key:refresh');