-- 告警静默规则表
CREATE TABLE IF NOT EXISTS alert_silence (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    match_condition TEXT,
    silence_start TIMESTAMP,
    silence_end TIMESTAMP,
    duration_seconds INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_alert_silence_enabled ON alert_silence(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_silence_time_range ON alert_silence(silence_start, silence_end);
CREATE INDEX IF NOT EXISTS idx_alert_silence_name ON alert_silence(name);

-- 添加注释
COMMENT ON TABLE alert_silence IS '告警静默规则配置';
COMMENT ON COLUMN alert_silence.id IS '主键ID';
COMMENT ON COLUMN alert_silence.name IS '静默规则名称';
COMMENT ON COLUMN alert_silence.description IS '描述';
COMMENT ON COLUMN alert_silence.match_condition IS '静默匹配条件（JSON格式）';
COMMENT ON COLUMN alert_silence.silence_start IS '静默开始时间';
COMMENT ON COLUMN alert_silence.silence_end IS '静默结束时间';
COMMENT ON COLUMN alert_silence.duration_seconds IS '静默时长（秒）';
COMMENT ON COLUMN alert_silence.enabled IS '是否启用';
COMMENT ON COLUMN alert_silence.created_by IS '创建者';
COMMENT ON COLUMN alert_silence.created_at IS '创建时间';
COMMENT ON COLUMN alert_silence.updated_at IS '更新时间';