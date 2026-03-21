-- Agent 指标数据表
CREATE TABLE agent_metric (
    id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    host_id UUID,
    type VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    tags TEXT,
    collected_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_agent_metric_agent_id ON agent_metric(agent_id);
CREATE INDEX idx_agent_metric_host_id ON agent_metric(host_id);
CREATE INDEX idx_agent_metric_type ON agent_metric(type);
CREATE INDEX idx_agent_metric_collected_at ON agent_metric(collected_at);
CREATE INDEX idx_agent_metric_agent_collected ON agent_metric(agent_id, collected_at);
CREATE INDEX idx_agent_metric_host_collected ON agent_metric(host_id, collected_at);

-- 分区表（可选，用于大数据量场景）
-- CREATE INDEX idx_agent_metric_composite ON agent_metric(agent_id, type, collected_at);