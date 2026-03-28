-- 通知渠道表
CREATE TABLE notification_channel (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    config TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_notification_channel_name UNIQUE (name)
);

-- 通知模板表
CREATE TABLE notification_template (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    variables TEXT,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_notification_template_name UNIQUE (name)
);

-- 告警规则表
CREATE TABLE alert_rule (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    metric VARCHAR(255) NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    threshold VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    notification_channel_ids TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_alert_rule_name UNIQUE (name)
);

-- 通知历史表
CREATE TABLE notification_history (
    id UUID PRIMARY KEY,
    channel_id UUID NOT NULL,
    template_id UUID,
    recipient VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_notification_channel_type ON notification_channel(type);
CREATE INDEX idx_notification_channel_enabled ON notification_channel(enabled);
CREATE INDEX idx_notification_template_type ON notification_template(type);
CREATE INDEX idx_notification_template_channel_type ON notification_template(channel_type);
CREATE INDEX idx_alert_rule_metric ON alert_rule(metric);
CREATE INDEX idx_alert_rule_enabled ON alert_rule(enabled);
CREATE INDEX idx_notification_history_channel_id ON notification_history(channel_id);
CREATE INDEX idx_notification_history_status ON notification_history(status);
CREATE INDEX idx_notification_history_created_at ON notification_history(created_at);