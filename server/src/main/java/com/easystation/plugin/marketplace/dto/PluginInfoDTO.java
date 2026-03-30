package com.easystation.plugin.marketplace.dto;

import java.time.Instant;
import java.util.List;

/**
 * 插件信息 DTO
 */
public record PluginInfoDTO(
    String id,
    String name,
    String version,
    String description,
    String author,
    String category,
    List<String> tags,
    int downloadCount,
    double rating,
    int reviewCount,
    String status,
    Instant installedAt
) {
    public static PluginInfoDTO of(
        String id, String name, String version, String description,
        String author, String category, List<String> tags,
        int downloadCount, double rating, int reviewCount, String status
    ) {
        return new PluginInfoDTO(id, name, version, description, author, 
            category, tags, downloadCount, rating, reviewCount, status, null);
    }
}
