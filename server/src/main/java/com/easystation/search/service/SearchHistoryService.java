package com.easystation.search.service;

import com.easystation.search.domain.UserSearchHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 搜索历史服务
 */
@ApplicationScoped
public class SearchHistoryService {

    /**
     * 记录搜索历史
     */
    @Transactional
    public void recordSearch(UUID userId, String query, int resultCount) {
        UserSearchHistory history = new UserSearchHistory();
        history.id = UUID.randomUUID();
        history.userId = userId;
        history.query = query;
        history.resultCount = resultCount;
        history.createdAt = LocalDateTime.now();
        history.persist();
    }

    /**
     * 获取用户搜索历史
     */
    @Transactional
    public List<UserSearchHistory> getHistory(UUID userId, int limit) {
        return UserSearchHistory.findByUserId(userId, limit);
    }

    /**
     * 清空用户搜索历史
     */
    @Transactional
    public void clearHistory(UUID userId) {
        UserSearchHistory.delete("userId", userId);
    }

    /**
     * 删除单条搜索历史
     */
    @Transactional
    public boolean deleteHistory(UUID historyId) {
        UserSearchHistory history = UserSearchHistory.findById(historyId);
        if (history != null) {
            history.delete();
            return true;
        }
        return false;
    }

    /**
     * 获取搜索历史数量
     */
    @Transactional
    public long countHistory(UUID userId) {
        return UserSearchHistory.count("userId", userId);
    }
}
