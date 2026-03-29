package com.easystation.search.service;

import com.easystation.search.domain.SearchResult;
import com.easystation.search.domain.SearchSuggestion;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchService 单元测试
 */
@QuarkusTest
public class SearchServiceTest {

    @Inject
    SearchService searchService;

    @Test
    void testSearchWithEmptyQuery() {
        List<SearchResult> results = searchService.search("", null, 20, 0);
        assertTrue(results.isEmpty());

        results = searchService.search(null, null, 20, 0);
        assertTrue(results.isEmpty());

        results = searchService.search("   ", null, 20, 0);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchWithValidQuery() {
        List<SearchResult> results = searchService.search("test", null, 20, 0);
        assertNotNull(results);
        // 搜索应该返回结果（可能为空列表，但不应为 null）
    }

    @Test
    void testSearchWithSpecificTypes() {
        List<SearchResult> results = searchService.search("host", "host,deployment", 10, 0);
        assertNotNull(results);
    }

    @Test
    void testSearchWithLimit() {
        List<SearchResult> results = searchService.search("test", null, 5, 0);
        assertNotNull(results);
        // 结果数量不应超过限制
        assertTrue(results.size() <= 5);
    }

    @Test
    void testGetSuggestionsWithEmptyQuery() {
        List<SearchSuggestion> suggestions = searchService.getSuggestions("", 5);
        assertTrue(suggestions.isEmpty());

        suggestions = searchService.getSuggestions(null, 5);
        assertTrue(suggestions.isEmpty());

        suggestions = searchService.getSuggestions("a", 5); // 单字符
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testGetSuggestionsWithValidQuery() {
        List<SearchSuggestion> suggestions = searchService.getSuggestions("ho", 5);
        assertNotNull(suggestions);
        // 应该有 "host" 建议
        assertTrue(suggestions.stream().anyMatch(s -> s.text().equals("host")));
    }

    @Test
    void testGetSuggestionsLimit() {
        List<SearchSuggestion> suggestions = searchService.getSuggestions("", 2);
        // 空查询返回空列表
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testCountResults() {
        long count = searchService.countResults("", null);
        assertEquals(0, count);

        count = searchService.countResults("test", null);
        assertTrue(count >= 0);
    }
}
