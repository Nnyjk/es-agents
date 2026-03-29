package com.easystation.plugin.marketplace.dto;

/**
 * 插件搜索请求
 */
public record PluginSearchRequest(
    String keyword,
    String category,
    java.util.List<String> tags,
    Integer page,
    Integer pageSize,
    String sortBy,
    String sortOrder
) {
    public PluginSearchRequest {
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;
        if (sortBy == null) sortBy = "name";
        if (sortOrder == null) sortOrder = "asc";
    }
    
    public int getPage() {
        return page;
    }
    
    public int getPageSize() {
        return pageSize;
    }
}
