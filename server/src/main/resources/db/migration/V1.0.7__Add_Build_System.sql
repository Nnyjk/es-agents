-- 构建任务表
CREATE TABLE build_task (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    template_id UUID,
    config TEXT,
    script TEXT,
    artifact_path VARCHAR(500),
    artifact_size BIGINT,
    version VARCHAR(100),
    logs TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    duration BIGINT,
    triggered_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 构建产物表
CREATE TABLE build_artifact (
    id UUID PRIMARY KEY,
    build_task_id UUID NOT NULL REFERENCES build_task(id) ON DELETE CASCADE,
    template_id UUID,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    checksum VARCHAR(128),
    checksum_type VARCHAR(20),
    latest BOOLEAN NOT NULL DEFAULT TRUE,
    download_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_build_task_type ON build_task(type);
CREATE INDEX idx_build_task_status ON build_task(status);
CREATE INDEX idx_build_task_template_id ON build_task(template_id);
CREATE INDEX idx_build_task_created_at ON build_task(created_at);
CREATE INDEX idx_build_artifact_build_task_id ON build_artifact(build_task_id);
CREATE INDEX idx_build_artifact_template_id ON build_artifact(template_id);
CREATE INDEX idx_build_artifact_version ON build_artifact(version);
CREATE INDEX idx_build_artifact_latest ON build_artifact(latest);