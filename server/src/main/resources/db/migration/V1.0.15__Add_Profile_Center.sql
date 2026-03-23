-- V1.0.15 Add Profile Center Tables

-- Extend sys_user table with profile fields
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS nickname VARCHAR(255);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS avatar VARCHAR(500);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS mfa_secret VARCHAR(255);

-- User Session table for login device management
CREATE TABLE IF NOT EXISTS user_session (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_id VARCHAR(255) NOT NULL,
    device_info VARCHAR(500),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    login_at TIMESTAMP(6),
    last_activity_at TIMESTAMP(6),
    expires_at TIMESTAMP(6),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_session_user_id ON user_session(user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_token_id ON user_session(token_id);

-- User Preference table
CREATE TABLE IF NOT EXISTS user_preference (
    id UUID NOT NULL,
    user_id UUID NOT NULL UNIQUE,
    theme VARCHAR(50) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'zh-CN',
    layout VARCHAR(50) DEFAULT 'default',
    default_page VARCHAR(255),
    page_size INTEGER DEFAULT 20,
    default_sort VARCHAR(255),
    display_fields TEXT,
    quick_actions TEXT,
    notification_enabled BOOLEAN DEFAULT TRUE,
    email_notification BOOLEAN DEFAULT TRUE,
    sms_notification BOOLEAN DEFAULT FALSE,
    webhook_notification BOOLEAN DEFAULT FALSE,
    silent_hours_start TIME,
    silent_hours_end TIME,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_preference_user_id ON user_preference(user_id);

-- User Notification Subscription table
CREATE TABLE IF NOT EXISTS user_notification_subscription (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    channels VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    UNIQUE(user_id, notification_type)
);

CREATE INDEX IF NOT EXISTS idx_user_notification_sub_user_id ON user_notification_subscription(user_id);

-- User Audit Log table for personal operation logs
CREATE TABLE IF NOT EXISTS user_audit_log (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(255),
    description TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    request_data TEXT,
    response_data TEXT,
    duration_ms BIGINT,
    created_at TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_audit_log_user_id ON user_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_user_audit_log_created_at ON user_audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_user_audit_log_action ON user_audit_log(action);

-- Password History table for security
CREATE TABLE IF NOT EXISTS user_password_history (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_password_history_user_id ON user_password_history(user_id);

-- Insert default user preferences for existing users
INSERT INTO user_preference (id, user_id, theme, language, created_at, updated_at)
SELECT gen_random_uuid(), id, 'light', 'zh-CN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM sys_user
WHERE NOT EXISTS (SELECT 1 FROM user_preference up WHERE up.user_id = sys_user.id);