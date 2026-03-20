-- API Token 表
CREATE TABLE sys_api_token (
    id UUID PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    user_id UUID,
    description VARCHAR(500),
    scope VARCHAR(50) NOT NULL DEFAULT 'READ_ONLY',
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revoked_by VARCHAR(255),
    revoked_reason VARCHAR(500),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- API Token 访问日志表
CREATE TABLE sys_api_token_access_log (
    id UUID PRIMARY KEY,
    token_id UUID NOT NULL REFERENCES sys_api_token(id) ON DELETE CASCADE,
    access_time TIMESTAMP NOT NULL,
    client_ip VARCHAR(50),
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    response_status INTEGER,
    response_time_ms BIGINT,
    request_body TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_api_token_token ON sys_api_token(token);
CREATE INDEX idx_api_token_user_id ON sys_api_token(user_id);
CREATE INDEX idx_api_token_revoked ON sys_api_token(is_revoked);
CREATE INDEX idx_api_token_expires_at ON sys_api_token(expires_at);
CREATE INDEX idx_api_token_log_token_id ON sys_api_token_access_log(token_id);
CREATE INDEX idx_api_token_log_access_time ON sys_api_token_access_log(access_time);
CREATE INDEX idx_api_token_log_client_ip ON sys_api_token_access_log(client_ip);