-- 告警渠道表
CREATE TABLE alert_channel (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    config TEXT,
    receivers TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 告警规则表
CREATE TABLE alert_rule (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    condition TEXT,
    environment_ids TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 告警规则-渠道关联表
CREATE TABLE alert_rule_channels (
    rule_id UUID NOT NULL REFERENCES alert_rule(id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES alert_channel(id) ON DELETE CASCADE,
    PRIMARY KEY (rule_id, channel_id)
);

-- 告警事件表
CREATE TABLE alert_event (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    title VARCHAR(500) NOT NULL,
    message TEXT,
    resource_id UUID,
    resource_type VARCHAR(100),
    environment_id UUID,
    rule_id UUID REFERENCES alert_rule(id) ON DELETE SET NULL,
    count INTEGER NOT NULL DEFAULT 1,
    last_notified_at TIMESTAMP,
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP,
    resolved_by VARCHAR(255),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_alert_channel_type ON alert_channel(type);
CREATE INDEX idx_alert_channel_enabled ON alert_channel(enabled);
CREATE INDEX idx_alert_rule_event_type ON alert_rule(event_type);
CREATE INDEX idx_alert_rule_enabled ON alert_rule(enabled);
CREATE INDEX idx_alert_event_event_type ON alert_event(event_type);
CREATE INDEX idx_alert_event_level ON alert_event(level);
CREATE INDEX idx_alert_event_status ON alert_event(status);
CREATE INDEX idx_alert_event_environment_id ON alert_event(environment_id);
CREATE INDEX idx_alert_event_resource_id ON alert_event(resource_id);
CREATE INDEX idx_alert_event_created_at ON alert_event(created_at);