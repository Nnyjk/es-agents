-- V202603292130__audit_log_enhancements.sql
-- 审计日志增强 - 添加新字段和索引

-- 1. 添加敏感操作标记字段
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS is_sensitive BOOLEAN DEFAULT FALSE;

-- 2. 添加风险等级字段
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS risk_level VARCHAR(20) DEFAULT 'LOW';
-- 风险等级：LOW, MEDIUM, HIGH, CRITICAL

-- 3. 添加操作分类字段
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS operation_category VARCHAR(50);
-- 操作分类：LOGIN, LOGOUT, DATA_ACCESS, DATA_MODIFY, ADMIN, SECURITY, SYSTEM

-- 4. 添加防篡改签名字段
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS signature VARCHAR(256);

-- 5. 添加防篡改哈希字段
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS hash VARCHAR(64);

-- 6. 添加审查相关字段
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS requires_review BOOLEAN DEFAULT FALSE;
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS review_status VARCHAR(20) DEFAULT 'PENDING';
-- 审查状态：PENDING, REVIEWED, FLAGGED
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS review_notes TEXT;
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS reviewer_id UUID;
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;

-- 7. 添加操作持续时间字段（毫秒）
ALTER TABLE user_audit_logs ADD COLUMN IF NOT EXISTS duration_ms INTEGER;

-- 8. 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_audit_logs_is_sensitive ON user_audit_logs(is_sensitive);
CREATE INDEX IF NOT EXISTS idx_audit_logs_risk_level ON user_audit_logs(risk_level);
CREATE INDEX IF NOT EXISTS idx_audit_logs_operation_category ON user_audit_logs(operation_category);
CREATE INDEX IF NOT EXISTS idx_audit_logs_requires_review ON user_audit_logs(requires_review);
CREATE INDEX IF NOT EXISTS idx_audit_logs_review_status ON user_audit_logs(review_status);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at_desc ON user_audit_logs(created_at DESC);

-- 9. 创建组合索引
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_risk_time ON user_audit_logs(user_id, risk_level, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_category_time ON user_audit_logs(operation_category, created_at);

-- 10. 更新现有日志的风险等级（基于操作类型）
-- 安全相关操作标记为 HIGH 风险
UPDATE user_audit_logs 
SET risk_level = 'HIGH', 
    operation_category = 'SECURITY',
    is_sensitive = TRUE
WHERE action IN ('LOGIN_FAILED', 'PASSWORD_CHANGE', 'TOKEN_REVOKE', 'PERMISSION_CHANGE', 'ROLE_ASSIGN');

-- 登录/登出操作
UPDATE user_audit_logs 
SET operation_category = 'LOGIN'
WHERE action IN ('LOGIN', 'LOGOUT');

-- 数据访问操作
UPDATE user_audit_logs 
SET operation_category = 'DATA_ACCESS'
WHERE action IN ('READ', 'QUERY', 'EXPORT', 'DOWNLOAD');

-- 数据修改操作
UPDATE user_audit_logs 
SET operation_category = 'DATA_MODIFY'
WHERE action IN ('CREATE', 'UPDATE', 'DELETE', 'BULK_OPERATION');

-- 管理操作
UPDATE user_audit_logs 
SET operation_category = 'ADMIN', is_sensitive = TRUE
WHERE action IN ('USER_CREATE', 'USER_DELETE', 'CONFIG_CHANGE', 'SYSTEM_CHANGE');

-- 系统操作
UPDATE user_audit_logs 
SET operation_category = 'SYSTEM'
WHERE action IN ('STARTUP', 'SHUTDOWN', 'BACKUP', 'MAINTENANCE');

COMMENT ON COLUMN user_audit_logs.is_sensitive IS '是否为敏感操作';
COMMENT ON COLUMN user_audit_logs.risk_level IS '风险等级：LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN user_audit_logs.operation_category IS '操作分类：LOGIN, LOGOUT, DATA_ACCESS, DATA_MODIFY, ADMIN, SECURITY, SYSTEM';
COMMENT ON COLUMN user_audit_logs.signature IS '防篡改签名 (HMAC-SHA256)';
COMMENT ON COLUMN user_audit_logs.hash IS '日志内容哈希 (SHA-256)';
COMMENT ON COLUMN user_audit_logs.requires_review IS '是否需要审查';
COMMENT ON COLUMN user_audit_logs.review_status IS '审查状态：PENDING, REVIEWED, FLAGGED';
COMMENT ON COLUMN user_audit_logs.review_notes IS '审查备注';
COMMENT ON COLUMN user_audit_logs.reviewer_id IS '审查人 ID';
COMMENT ON COLUMN user_audit_logs.reviewed_at IS '审查时间';
COMMENT ON COLUMN user_audit_logs.duration_ms IS '操作持续时间 (毫秒)';
