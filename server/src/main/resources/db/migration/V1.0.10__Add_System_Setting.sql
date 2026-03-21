-- 系统设置表
CREATE TABLE system_setting (
    id UUID PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    value TEXT,
    category VARCHAR(50),
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_system_setting_category ON system_setting(category);
CREATE INDEX idx_system_setting_key ON system_setting(key);

-- 初始化默认设置
INSERT INTO system_setting (id, key, value, category, description, created_at, updated_at) VALUES
    (gen_random_uuid(), 'platformName', 'Easy-Station', 'basic', '平台名称', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'passwordMinLength', '8', 'security', '密码最小长度', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'passwordRequireUppercase', 'true', 'security', '密码要求大写字母', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'passwordRequireLowercase', 'true', 'security', '密码要求小写字母', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'passwordRequireNumber', 'true', 'security', '密码要求数字', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'passwordRequireSpecial', 'false', 'security', '密码要求特殊字符', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'sessionTimeoutMinutes', '30', 'security', '会话超时时间(分钟)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'maxLoginAttempts', '5', 'security', '最大登录尝试次数', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'alertEnabled', 'true', 'alert', '告警启用开关', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'alertConvergenceSeconds', '60', 'alert', '告警收敛时间(秒)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'alertRetryCount', '3', 'alert', '告警重试次数', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);