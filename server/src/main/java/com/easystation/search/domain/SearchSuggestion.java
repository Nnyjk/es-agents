package com.easystation.search.domain;

/**
 * 搜索建议 DTO
 */
public record SearchSuggestion(
    String text,
    String type
) {
}
