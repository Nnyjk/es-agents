-- 站内消息表
CREATE TABLE notification_messages (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,           -- 接收用户 ID
    username VARCHAR(100) NOT NULL,  -- 接收用户名
    title VARCHAR(200) NOT NULL,     -- 消息标题
    content TEXT NOT NULL,           -- 消息内容
    type VARCHAR(50) NOT NULL,       -- 消息类型：SYSTEM/ALERT/OPERATION
    level VARCHAR(20),               -- 消息级别：INFO/WARNING/ERROR
    is_read BOOLEAN DEFAULT FALSE,   -- 是否已读
    related_type VARCHAR(50),        -- 关联资源类型：HOST/AGENT/DEPLOYMENT/ALERT
    related_id UUID,                 -- 关联资源 ID
    jump_url VARCHAR(500),           -- 跳转链接
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,               -- 阅读时间
    deleted BOOLEAN DEFAULT FALSE    -- 软删除标记
);

-- 索引
CREATE INDEX idx_notification_user_read ON notification_messages(user_id, is_read);
CREATE INDEX idx_notification_created ON notification_messages(created_at DESC);
CREATE INDEX idx_notification_deleted ON notification_messages(deleted);
