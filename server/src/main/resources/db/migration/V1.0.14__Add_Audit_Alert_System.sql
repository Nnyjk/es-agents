-- 审计告警配置表
CREATE TABLE audit_alert_config (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    alert_type VARCHAR(50) NOT NULL,
    failure_threshold INTEGER,
    time_window_minutes INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_audit_alert_config_name UNIQUE (name)
);

-- 审计告警配置敏感操作列表
CREATE TABLE audit_alert_sensitive_actions (
    config_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    CONSTRAINT fk_audit_alert_sensitive_actions_config FOREIGN KEY (config_id) REFERENCES audit_alert_config(id) ON DELETE CASCADE
);

-- 审计告警配置白名单用户列表
CREATE TABLE audit_alert_whitelist_users (
    config_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    CONSTRAINT fk_audit_alert_whitelist_users_config FOREIGN KEY (config_id) REFERENCES audit_alert_config(id) ON DELETE CASCADE
);

-- 审计告警配置通知渠道列表
CREATE TABLE audit_alert_notify_channels (
    config_id UUID NOT NULL,
    channel VARCHAR(50) NOT NULL,
    CONSTRAINT fk_audit_alert_notify_channels_config FOREIGN KEY (config_id) REFERENCES audit_alert_config(id) ON DELETE CASCADE
);

-- 审计告警历史表
CREATE TABLE audit_alert_history (
    id UUID PRIMARY KEY,
    config_id UUID,
    alert_name VARCHAR(255) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    trigger_user VARCHAR(255),
    trigger_ip VARCHAR(50),
    detail TEXT,
    notify_channel VARCHAR(255),
    notify_status VARCHAR(20),
    notify_error VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_audit_alert_history_config FOREIGN KEY (config_id) REFERENCES audit_alert_config(id) ON DELETE SET NULL
);

-- 审计告警历史相关记录列表
CREATE TABLE audit_alert_related_records (
    alert_id UUID NOT NULL,
    record_id UUID NOT NULL,
    CONSTRAINT fk_audit_alert_related_records_alert FOREIGN KEY (alert_id) REFERENCES audit_alert_history(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_audit_alert_config_type ON audit_alert_config(alert_type);
CREATE INDEX idx_audit_alert_config_enabled ON audit_alert_config(enabled);

CREATE INDEX idx_audit_alert_history_type ON audit_alert_history(alert_type);
CREATE INDEX idx_audit_alert_history_status ON audit_alert_history(status);
CREATE INDEX idx_audit_alert_history_created ON audit_alert_history(created_at);
CREATE INDEX idx_audit_alert_history_user ON audit_alert_history(trigger_user);
CREATE INDEX idx_audit_alert_history_config ON audit_alert_history(config_id);

-- 初始数据：敏感操作告警配置
INSERT INTO audit_alert_config (id, name, description, alert_type, enabled, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    '敏感操作告警',
    '当检测到敏感操作时触发告警',
    'SENSITIVE_OPERATION',
    true,
    NOW()
);

-- 初始数据：失败操作告警配置
INSERT INTO audit_alert_config (id, name, description, alert_type, failure_threshold, time_window_minutes, enabled, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000002',
    '失败操作告警',
    '当用户在指定时间内失败操作次数超过阈值时触发告警',
    'FAILED_OPERATION',
    5,
    5,
    true,
    NOW()
);

-- 初始数据：频繁访问告警配置
INSERT INTO audit_alert_config (id, name, description, alert_type, failure_threshold, time_window_minutes, enabled, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000003',
    '频繁访问告警',
    '当用户在指定时间内访问次数超过阈值时触发告警',
    'FREQUENT_ACCESS',
    100,
    1,
    true,
    NOW()
);