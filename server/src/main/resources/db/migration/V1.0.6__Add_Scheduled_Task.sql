-- 定时任务表
CREATE TABLE scheduled_task (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    cron_expression VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    config TEXT,
    target_id UUID,
    target_type VARCHAR(100),
    max_retries INTEGER DEFAULT 3,
    timeout_seconds INTEGER DEFAULT 300,
    alert_on_failure BOOLEAN NOT NULL DEFAULT TRUE,
    last_execution_at TIMESTAMP,
    last_execution_status VARCHAR(50),
    next_execution_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 任务执行记录表
CREATE TABLE task_execution (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES scheduled_task(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration_ms BIGINT,
    scheduled_at TIMESTAMP,
    trigger_type VARCHAR(50),
    triggered_by VARCHAR(255),
    result TEXT,
    logs TEXT,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_scheduled_task_status ON scheduled_task(status);
CREATE INDEX idx_scheduled_task_type ON scheduled_task(type);
CREATE INDEX idx_scheduled_task_next_execution ON scheduled_task(next_execution_at);
CREATE INDEX idx_task_execution_task_id ON task_execution(task_id);
CREATE INDEX idx_task_execution_status ON task_execution(status);
CREATE INDEX idx_task_execution_scheduled_at ON task_execution(scheduled_at);
CREATE INDEX idx_task_execution_created_at ON task_execution(created_at);