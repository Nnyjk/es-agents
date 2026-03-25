-- V1.0.16 Add Auth Enhancement
-- 用户认证增强：刷新令牌、登录失败记录

-- 刷新令牌表
CREATE TABLE sys_refresh_token (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token_hash varchar(255) NOT NULL UNIQUE,
    device_info varchar(500),
    ip_address varchar(50),
    user_agent varchar(500),
    expires_at timestamp(6) NOT NULL,
    created_at timestamp(6),
    last_used_at timestamp(6),
    is_revoked boolean DEFAULT false,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user ON sys_refresh_token(user_id);
CREATE INDEX idx_refresh_token_hash ON sys_refresh_token(token_hash);
CREATE INDEX idx_refresh_token_expires ON sys_refresh_token(expires_at);

-- 登录失败记录表
CREATE TABLE sys_login_attempt (
    id uuid NOT NULL,
    username varchar(255),
    ip_address varchar(50),
    user_agent varchar(500),
    success boolean NOT NULL,
    fail_reason varchar(255),
    created_at timestamp(6),
    PRIMARY KEY (id)
);

CREATE INDEX idx_login_attempt_username ON sys_login_attempt(username);
CREATE INDEX idx_login_attempt_ip ON sys_login_attempt(ip_address);
CREATE INDEX idx_login_attempt_created ON sys_login_attempt(created_at);

-- 密码重置令牌表
CREATE TABLE sys_password_reset_token (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token_hash varchar(255) NOT NULL UNIQUE,
    email varchar(255),
    phone varchar(50),
    expires_at timestamp(6) NOT NULL,
    used_at timestamp(6),
    created_at timestamp(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user ON sys_password_reset_token(user_id);
CREATE INDEX idx_password_reset_token ON sys_password_reset_token(token_hash);
CREATE INDEX idx_password_reset_expires ON sys_password_reset_token(expires_at);

-- 为 sys_user 表添加登录失败计数字段
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS failed_login_count integer DEFAULT 0;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS locked_until timestamp(6);

COMMENT ON TABLE sys_refresh_token IS '刷新令牌表';
COMMENT ON TABLE sys_login_attempt IS '登录尝试记录表';
COMMENT ON TABLE sys_password_reset_token IS '密码重置令牌表';
COMMENT ON COLUMN sys_user.failed_login_count IS '连续登录失败次数';
COMMENT ON COLUMN sys_user.locked_until IS '账户锁定截止时间';