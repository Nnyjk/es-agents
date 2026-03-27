-- 重命名 AgentMonitoringMetric 表
-- 修复 AgentMetric 实体重复映射到同一表的问题

-- 创建 agent_monitoring_metric 表（用于 agent/domain/AgentMetric.java）
-- 这是具体的监控指标表，不同于通用的 agent_metric 表

CREATE TABLE IF NOT EXISTS agent_monitoring_metric (
    id UUID PRIMARY KEY,
    agent_instance_id UUID NOT NULL REFERENCES agent_instance(id),
    cpu_usage NUMERIC(5, 2),
    memory_usage NUMERIC(5, 2),
    disk_usage NUMERIC(5, 2),
    network_in_bytes BIGINT,
    network_out_bytes BIGINT,
    process_count INTEGER,
    thread_count INTEGER,
    uptime_seconds BIGINT,
    collected_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_agent_monitoring_metric_agent ON agent_monitoring_metric(agent_instance_id);
CREATE INDEX idx_agent_monitoring_metric_time ON agent_monitoring_metric(collected_at);