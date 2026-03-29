package com.easystation.search.repository;

import com.easystation.search.domain.UserSearchHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 搜索历史 Repository
 */
@ApplicationScoped
public class SearchHistoryRepository {

    /**
     * 获取用户的搜索历史
     */
    public List<UserSearchHistory> getHistory(UUID userId, int limit) {
        return UserSearchHistory.findByUserId(userId, limit);
    }

    /**
     * 添加搜索历史
     */
    @Transactional
    public UserSearchHistory addHistory(UUID userId, String query, Integer resultCount) {
        UserSearchHistory history = new UserSearchHistory();
        history.id = UUID.randomUUID();
        history.userId = userId;
        history.query = query;
        history.resultCount = resultCount;
        history.createdAt = LocalDateTime.now();
        history.persist();
        return history;
    }

    /**
     * 删除搜索历史
     */
    @Transactional
    public boolean deleteHistory(UUID userId, UUID historyId) {
        long deleted = UserSearchHistory.deleteByUserIdAndId(userId, historyId);
        return deleted > 0;
    }

    /**
     * 清空用户的搜索历史
     */
    @Transactional
    public long clearHistory(UUID userId) {
        return UserSearchHistory.deleteByUserId(userId);
    }
}
