-- V15__create_plugin_marketplace.sql
-- 插件市场数据库表

-- 插件分类表
CREATE TABLE plugin_category (
    id UUID PRIMARY KEY,
    parent_id UUID,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    icon VARCHAR(500),
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_category_parent ON plugin_category(parent_id);
CREATE INDEX idx_plugin_category_code ON plugin_category(code);
CREATE INDEX idx_plugin_category_active ON plugin_category(is_active);

-- 插件表
CREATE TABLE plugin (
    id UUID PRIMARY KEY,
    developer_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE,
    icon VARCHAR(500),
    description TEXT,
    readme TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    download_count INTEGER DEFAULT 0,
    install_count INTEGER DEFAULT 0,
    rating_avg DECIMAL(2,1) DEFAULT 0.0,
    rating_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_developer ON plugin(developer_id);
CREATE INDEX idx_plugin_category ON plugin(category_id);
CREATE INDEX idx_plugin_code ON plugin(code);
CREATE INDEX idx_plugin_status ON plugin(status);
CREATE INDEX idx_plugin_name ON plugin(name);

-- 插件版本表
CREATE TABLE plugin_version (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    version VARCHAR(20) NOT NULL,
    version_code INTEGER,
    changelog TEXT,
    download_url VARCHAR(500),
    package_size BIGINT,
    checksum VARCHAR(100),
    status VARCHAR(20) DEFAULT 'DRAFT',
    is_latest BOOLEAN DEFAULT FALSE,
    min_agent_version VARCHAR(20),
    max_agent_version VARCHAR(20),
    dependencies JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plugin_id, version)
);

CREATE INDEX idx_plugin_version_plugin ON plugin_version(plugin_id);
CREATE INDEX idx_plugin_version_latest ON plugin_version(is_latest);
CREATE INDEX idx_plugin_version_status ON plugin_version(status);

-- 插件审核表
CREATE TABLE plugin_review (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    version_id UUID,
    reviewer_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    review_type VARCHAR(20),
    comments TEXT,
    rejected_reason TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_review_plugin ON plugin_review(plugin_id);
CREATE INDEX idx_plugin_review_version ON plugin_review(version_id);
CREATE INDEX idx_plugin_review_reviewer ON plugin_review(reviewer_id);
CREATE INDEX idx_plugin_review_status ON plugin_review(status);

-- 插件评论表
CREATE TABLE plugin_comment (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    user_id UUID NOT NULL,
    parent_id UUID,
    reply_to_user_id UUID,
    content TEXT NOT NULL,
    is_developer_reply BOOLEAN DEFAULT FALSE,
    is_hidden BOOLEAN DEFAULT FALSE,
    like_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_comment_plugin ON plugin_comment(plugin_id);
CREATE INDEX idx_plugin_comment_user ON plugin_comment(user_id);
CREATE INDEX idx_plugin_comment_parent ON plugin_comment(parent_id);
CREATE INDEX idx_plugin_comment_hidden ON plugin_comment(is_hidden);

-- 插件评分表
CREATE TABLE plugin_rating (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating DECIMAL(2,1) NOT NULL,
    review TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plugin_id, user_id)
);

CREATE INDEX idx_plugin_rating_plugin ON plugin_rating(plugin_id);
CREATE INDEX idx_plugin_rating_user ON plugin_rating(user_id);
CREATE INDEX idx_plugin_rating_verified ON plugin_rating(is_verified);

-- 插件安装表
CREATE TABLE plugin_installation (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    version_id UUID,
    agent_id UUID,
    user_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'INSTALLING',
    installed_at TIMESTAMP,
    uninstalled_at TIMESTAMP,
    config JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_installation_plugin ON plugin_installation(plugin_id);
CREATE INDEX idx_plugin_installation_agent ON plugin_installation(agent_id);
CREATE INDEX idx_plugin_installation_user ON plugin_installation(user_id);
CREATE INDEX idx_plugin_installation_status ON plugin_installation(status);

-- 插件收藏表
CREATE TABLE plugin_favorite (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plugin_id, user_id)
);

CREATE INDEX idx_plugin_favorite_plugin ON plugin_favorite(plugin_id);
CREATE INDEX idx_plugin_favorite_user ON plugin_favorite(user_id);

-- 插件标签表
CREATE TABLE plugin_tag (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(50),
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_tag_name ON plugin_tag(name);

-- 插件标签关联表
CREATE TABLE plugin_tag_relation (
    id UUID PRIMARY KEY,
    plugin_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    UNIQUE(plugin_id, tag_id)
);

CREATE INDEX idx_plugin_tag_relation_plugin ON plugin_tag_relation(plugin_id);
CREATE INDEX idx_plugin_tag_relation_tag ON plugin_tag_relation(tag_id);

-- 初始数据：插件分类
INSERT INTO plugin_category (id, parent_id, name, code, icon, description, sort_order, is_active) VALUES
    (gen_random_uuid(), NULL, '数据处理', 'data-processing', 'database', '数据采集、清洗、转换等插件', 1, TRUE),
    (gen_random_uuid(), NULL, 'AI/ML', 'ai-ml', 'brain', '人工智能、机器学习相关插件', 2, TRUE),
    (gen_random_uuid(), NULL, '监控告警', 'monitoring', 'bell', '监控、告警、日志相关插件', 3, TRUE),
    (gen_random_uuid(), NULL, '部署运维', 'deployment', 'rocket', '部署、发布、运维相关插件', 4, TRUE),
    (gen_random_uuid(), NULL, '集成工具', 'integration', 'plug', '第三方系统集成插件', 5, TRUE),
    (gen_random_uuid(), NULL, '其他', 'other', 'cloud', '其他类型插件', 99, TRUE);
