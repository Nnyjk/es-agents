package com.easystation.search.domain;

import java.util.Map;

/**
 * 搜索结果 DTO
 */
public record SearchResult(
    String type,           // host, deployment, config, log
    String id,
    String name,
    String description,
    Double score,          // 相关性评分 0-1
    Map<String, String> highlights  // 高亮字段
) {
}
