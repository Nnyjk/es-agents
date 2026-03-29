-- 搜索历史表
CREATE TABLE user_search_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    query VARCHAR(500) NOT NULL,
    result_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_user_search_history_user_id ON user_search_history(user_id);
CREATE INDEX idx_user_search_history_created_at ON user_search_history(created_at);
