-- 配置项表
CREATE TABLE config_item (
    id UUID PRIMARY KEY,
    key VARCHAR(255) NOT NULL,
    value TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    description VARCHAR(500),
    environment_id UUID,
    "group" VARCHAR(100),
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_config_key_env UNIQUE (key, environment_id)
);

-- 配置历史表
CREATE TABLE config_history (
    id UUID PRIMARY KEY,
    config_id UUID NOT NULL,
    key VARCHAR(255) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_type VARCHAR(50) NOT NULL,
    version INTEGER,
    changed_by VARCHAR(255),
    change_reason VARCHAR(500),
    environment_id UUID,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_config_item_key ON config_item(key);
CREATE INDEX idx_config_item_environment_id ON config_item(environment_id);
CREATE INDEX idx_config_item_group ON config_item("group");
CREATE INDEX idx_config_item_active ON config_item(is_active);
CREATE INDEX idx_config_history_config_id ON config_history(config_id);
CREATE INDEX idx_config_history_key ON config_history(key);
CREATE INDEX idx_config_history_change_type ON config_history(change_type);
CREATE INDEX idx_config_history_changed_at ON config_history(changed_at);