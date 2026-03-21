-- Agent 资源版本表
CREATE TABLE agent_source_version (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES agent_source(id) ON DELETE CASCADE,
    version VARCHAR(100) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    checksum_md5 VARCHAR(64),
    checksum_sha256 VARCHAR(128),
    description TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP,
    verified_by VARCHAR(255),
    download_url VARCHAR(1000),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_source_version UNIQUE (source_id, version)
);

-- Agent 资源缓存表
CREATE TABLE agent_source_cache (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES agent_source(id) ON DELETE CASCADE,
    version_id UUID REFERENCES agent_source_version(id) ON DELETE SET NULL,
    cache_path VARCHAR(500) NOT NULL,
    cache_size BIGINT,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    last_accessed_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_source_version_source_id ON agent_source_version(source_id);
CREATE INDEX idx_source_version_version ON agent_source_version(version);
CREATE INDEX idx_source_version_verified ON agent_source_version(is_verified);
CREATE INDEX idx_source_cache_source_id ON agent_source_cache(source_id);
CREATE INDEX idx_source_cache_valid ON agent_source_cache(is_valid);
CREATE INDEX idx_source_cache_expires ON agent_source_cache(expires_at);