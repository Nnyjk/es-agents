package com.easystation.search.service;

import com.easystation.search.domain.UserSearchHistory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchHistoryService 单元测试
 */
@QuarkusTest
public class SearchHistoryServiceTest {

    @Inject
    SearchHistoryService historyService;

    private UUID testUserId;

    @BeforeEach
    @Transactional
    void setUp() {
        testUserId = UUID.randomUUID();
        // 清理测试用户的旧数据
        historyService.clearHistory(testUserId);
    }

    @Test
    @Transactional
    void testRecordSearch() {
        historyService.recordSearch(testUserId, "test query", 10);

        List<UserSearchHistory> history = historyService.getHistory(testUserId, 10);
        assertEquals(1, history.size());
        assertEquals("test query", history.get(0).query);
        assertEquals(10, history.get(0).resultCount);
        assertNotNull(history.get(0).id);
        assertEquals(testUserId, history.get(0).userId);
    }

    @Test
    @Transactional
    void testGetHistory() {
        // 创建多条搜索历史
        for (int i = 0; i < 5; i++) {
            historyService.recordSearch(testUserId, "query " + i, i * 10);
        }

        List<UserSearchHistory> history = historyService.getHistory(testUserId, 10);
        assertEquals(5, history.size());

        // 测试 limit
        List<UserSearchHistory> limited = historyService.getHistory(testUserId, 3);
        assertEquals(3, limited.size());
    }

    @Test
    @Transactional
    void testClearHistory() {
        // 创建搜索历史
        for (int i = 0; i < 3; i++) {
            historyService.recordSearch(testUserId, "query " + i, 10);
        }

        assertEquals(3, historyService.countHistory(testUserId));

        // 清空历史
        historyService.clearHistory(testUserId);

        assertEquals(0, historyService.countHistory(testUserId));
    }

    @Test
    @Transactional
    void testDeleteHistory() {
        // 创建搜索历史
        historyService.recordSearch(testUserId, "query 1", 10);
        historyService.recordSearch(testUserId, "query 2", 20);

        List<UserSearchHistory> history = historyService.getHistory(testUserId, 10);
        assertEquals(2, history.size());

        // 删除第一条
        UUID firstId = history.get(0).id;
        boolean deleted = historyService.deleteHistory(firstId);
        assertTrue(deleted);

        assertEquals(1, historyService.countHistory(testUserId));

        // 删除不存在的记录
        assertFalse(historyService.deleteHistory(UUID.randomUUID()));
    }

    @Test
    @Transactional
    void testCountHistory() {
        assertEquals(0, historyService.countHistory(testUserId));

        historyService.recordSearch(testUserId, "query 1", 10);
        assertEquals(1, historyService.countHistory(testUserId));

        historyService.recordSearch(testUserId, "query 2", 20);
        assertEquals(2, historyService.countHistory(testUserId));
    }

    @Test
    @Transactional
    void testHistoryOrderByCreatedAt() {
        // 创建搜索历史（按时间顺序）
        historyService.recordSearch(testUserId, "first", 10);
        historyService.recordSearch(testUserId, "second", 20);
        historyService.recordSearch(testUserId, "third", 30);

        List<UserSearchHistory> history = historyService.getHistory(testUserId, 10);
        assertEquals(3, history.size());

        // 应该按创建时间倒序排列（最新的在前）
        assertEquals("third", history.get(0).query);
        assertEquals("second", history.get(1).query);
        assertEquals("first", history.get(2).query);
    }
}
