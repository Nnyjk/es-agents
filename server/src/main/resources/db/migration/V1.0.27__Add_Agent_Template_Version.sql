-- 创建 Agent 模板版本表
-- V1.0.27__Add_Agent_Template_Version.sql

CREATE TABLE IF NOT EXISTS agent_template_version (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES agent_template(id) ON DELETE CASCADE,
    version VARCHAR(50) NOT NULL,
    description TEXT,
    install_script TEXT,
    config_template TEXT,
    dependencies TEXT,
    os_type VARCHAR(20),
    arch_support VARCHAR(255),
    is_published BOOLEAN DEFAULT FALSE,
    is_latest BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    created_by VARCHAR(100),
    UNIQUE (template_id, version)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_template_version_template ON agent_template_version(template_id);
CREATE INDEX IF NOT EXISTS idx_template_version_latest ON agent_template_version(template_id, is_latest);
CREATE INDEX IF NOT EXISTS idx_template_version_published ON agent_template_version(is_published);

-- 迁移现有模板数据到版本表（将当前模板数据作为 v1.0.0 初始版本）
INSERT INTO agent_template_version (id, template_id, version, description, install_script, config_template, dependencies, os_type, arch_support, is_published, is_latest, created_at, created_by)
SELECT 
    gen_random_uuid(),
    t.id,
    '1.0.0',
    t.description,
    t.install_script,
    t.config_template,
    t.dependencies,
    t.os_type,
    t.arch_support,
    TRUE,
    TRUE,
    t.created_at,
    'system'
FROM agent_template t
WHERE NOT EXISTS (
    SELECT 1 FROM agent_template_version v WHERE v.template_id = t.id
);

COMMENT ON TABLE agent_template_version IS 'Agent 模板版本表 - 支持模板的多版本管理和版本历史';
COMMENT ON COLUMN agent_template_version.id IS '版本 ID';
COMMENT ON COLUMN agent_template_version.template_id IS '关联的模板 ID';
COMMENT ON COLUMN agent_template_version.version IS '版本号 (语义化版本，如 1.0.0)';
COMMENT ON COLUMN agent_template_version.description IS '版本描述';
COMMENT ON COLUMN agent_template_version.install_script IS '安装脚本';
COMMENT ON COLUMN agent_template_version.config_template IS '配置模板';
COMMENT ON COLUMN agent_template_version.dependencies IS '依赖配置 (JSON)';
COMMENT ON COLUMN agent_template_version.os_type IS '操作系统类型 (LINUX/WINDOWS/MACOS)';
COMMENT ON COLUMN agent_template_version.arch_support IS '支持的架构 (逗号分隔，如 x86_64,arm64)';
COMMENT ON COLUMN agent_template_version.is_published IS '是否已发布';
COMMENT ON COLUMN agent_template_version.is_latest IS '是否为最新版本';
COMMENT ON COLUMN agent_template_version.created_at IS '创建时间';
COMMENT ON COLUMN agent_template_version.published_at IS '发布时间';
COMMENT ON COLUMN agent_template_version.created_by IS '创建人';
