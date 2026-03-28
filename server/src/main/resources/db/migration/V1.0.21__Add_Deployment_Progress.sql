-- 部署进展表
CREATE TABLE deployment_progress (
    id UUID PRIMARY KEY,
    deployment_id UUID NOT NULL,
    stage VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    progress_percent INTEGER DEFAULT 0,
    message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 部署进展历史表
CREATE TABLE deployment_progress_history (
    id UUID PRIMARY KEY,
    deployment_id UUID NOT NULL,
    stage VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_deployment_progress_deployment_id ON deployment_progress(deployment_id);
CREATE INDEX idx_deployment_progress_status ON deployment_progress(status);
CREATE INDEX idx_deployment_progress_created_at ON deployment_progress(created_at);

CREATE INDEX idx_deployment_progress_history_deployment_id ON deployment_progress_history(deployment_id);
CREATE INDEX idx_deployment_progress_history_created_at ON deployment_progress_history(created_at);

-- 复合索引
CREATE INDEX idx_deployment_progress_deployment_status ON deployment_progress(deployment_id, status);