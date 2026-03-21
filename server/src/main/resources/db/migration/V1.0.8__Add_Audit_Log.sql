-- 审计日志表
CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    result VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    resource_type VARCHAR(100),
    resource_id UUID,
    details TEXT,
    request_params TEXT,
    response_result TEXT,
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    request_path VARCHAR(500),
    request_method VARCHAR(10),
    duration BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_audit_log_username ON audit_log(username);
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_result ON audit_log(result);
CREATE INDEX idx_audit_log_resource_type ON audit_log(resource_type);
CREATE INDEX idx_audit_log_resource_id ON audit_log(resource_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_client_ip ON audit_log(client_ip);

-- 复合索引
CREATE INDEX idx_audit_log_user_time ON audit_log(user_id, created_at);
CREATE INDEX idx_audit_log_action_time ON audit_log(action, created_at);

-- 注意：审计日志表不支持 UPDATE 和 DELETE 操作
-- 通过应用层限制，确保日志不可修改和删除