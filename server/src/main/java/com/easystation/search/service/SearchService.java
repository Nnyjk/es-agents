package com.easystation.search.service;

import com.easystation.search.domain.SearchResult;
import com.easystation.search.domain.SearchSuggestion;
import com.easystation.search.repository.SearchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 搜索服务 - 统一搜索逻辑
 */
@ApplicationScoped
public class SearchService {

    @Inject
    SearchRepository searchRepository;

    /**
     * 统一搜索
     * @param query 搜索关键词
     * @param types 搜索类型（host, deployment, config, log），null 表示全部
     * @param limit 结果数量限制
     * @param offset 偏移量
     * @return 搜索结果
     */
    @Transactional
    public List<SearchResult> search(String query, String types, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<String> typeList = types != null && !types.isBlank()
            ? Arrays.asList(types.split(","))
            : Arrays.asList("host", "deployment", "config", "log");

        return searchRepository.search(query.trim(), typeList, limit, offset);
    }

    /**
     * 获取搜索建议
     * @param query 搜索关键词前缀
     * @param limit 建议数量
     * @return 搜索建议列表
     */
    @Transactional
    public List<SearchSuggestion> getSuggestions(String query, int limit) {
        if (query == null || query.isBlank() || query.length() < 2) {
            return List.of();
        }

        List<SearchSuggestion> suggestions = new ArrayList<>();
        String prefix = query.toLowerCase().trim();

        // 从常见搜索词中获取建议（实际项目中可从 Redis 缓存或数据库获取）
        String[] commonSearches = {
            "host", "deployment", "config", "log",
            "nginx", "mysql", "redis", "kubernetes",
            "production", "staging", "development"
        };

        for (String common : commonSearches) {
            if (common.startsWith(prefix) && suggestions.size() < limit) {
                suggestions.add(new SearchSuggestion(common, "general"));
            }
        }

        return suggestions;
    }

    /**
     * 统计搜索结果数量
     */
    @Transactional
    public long countResults(String query, String types) {
        if (query == null || query.isBlank()) {
            return 0;
        }

        List<String> typeList = types != null && !types.isBlank()
            ? Arrays.asList(types.split(","))
            : Arrays.asList("host", "deployment", "config", "log");

        return searchRepository.countResults(query.trim(), typeList);
    }
}
