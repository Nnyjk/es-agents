-- Create config tables for hot reload support
-- Author: Y-Bot-N
-- Date: 2026-03-30

-- Config table: stores current configuration values
CREATE TABLE config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    config_type VARCHAR(50) DEFAULT 'STRING', -- STRING, NUMBER, BOOLEAN, JSON
    version INT DEFAULT 1,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Config history table: tracks all configuration changes
CREATE TABLE config_history (
    id BIGSERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_reason VARCHAR(500),
    FOREIGN KEY (config_id) REFERENCES config(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_config_key ON config(config_key);
CREATE INDEX idx_config_history_config_id ON config_history(config_id);
CREATE INDEX idx_config_history_changed_at ON config_history(changed_at);

-- Insert initial configurations
INSERT INTO config (config_key, config_value, description, config_type) VALUES
    ('app.name', 'Easy Station', 'Application name', 'STRING'),
    ('app.version', '1.0.0', 'Application version', 'STRING'),
    ('app.debug.enabled', 'false', 'Enable debug mode', 'BOOLEAN'),
    ('server.log.level', 'INFO', 'Server log level', 'STRING'),
    ('cache.ttl.seconds', '300', 'Default cache TTL in seconds', 'NUMBER');

COMMENT ON TABLE config IS 'Configuration table for hot reload support';
COMMENT ON TABLE config_history IS 'Configuration change history for audit and rollback';
