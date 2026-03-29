package com.easystation.plugin.marketplace.dto;

/**
 * 插件详情响应
 */
public record PluginDetailResponse(
    String id,
    String name,
    String version,
    String description,
    String author,
    String category,
    java.util.List<String> tags,
    int downloadCount,
    double rating,
    int reviewCount,
    String status,
    String downloadUrl,
    String homepage,
    String repository,
    java.util.List<String> screenshots,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static PluginDetailResponse of(
        String id, String name, String version, String description,
        String author, String category, java.util.List<String> tags,
        int downloadCount, double rating, int reviewCount, String status
    ) {
        return new PluginDetailResponse(id, name, version, description, author,
            category, tags, downloadCount, rating, reviewCount, status,
            null, null, null, null, null, null);
    }
}
