-- 流水线表
CREATE TABLE pipeline (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    environment_id UUID,
    template_id UUID,
    stages TEXT,
    trigger_config TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 流水线执行表
CREATE TABLE pipeline_execution (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL REFERENCES pipeline(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    trigger_type VARCHAR(50) NOT NULL,
    triggered_by VARCHAR(255),
    deployment_id UUID,
    version VARCHAR(100),
    logs TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration BIGINT,
    current_stage INTEGER NOT NULL DEFAULT 0,
    total_stages INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 阶段执行表
CREATE TABLE stage_execution (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL REFERENCES pipeline_execution(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    order_index INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    config TEXT,
    logs TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_pipeline_status ON pipeline(status);
CREATE INDEX idx_pipeline_environment_id ON pipeline(environment_id);
CREATE INDEX idx_pipeline_execution_pipeline_id ON pipeline_execution(pipeline_id);
CREATE INDEX idx_pipeline_execution_status ON pipeline_execution(status);
CREATE INDEX idx_pipeline_execution_created_at ON pipeline_execution(created_at);
CREATE INDEX idx_stage_execution_execution_id ON stage_execution(execution_id);
CREATE INDEX idx_stage_execution_status ON stage_execution(status);