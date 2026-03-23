package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.Plugin;
import com.easystation.plugin.dto.PluginRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginMapper {

    @Mapping(target = "developerId", source = "developer.id")
    @Mapping(target = "developerName", source = "developer.username")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "tags", expression = "java(entity.getTagsAsList())")
    @Mapping(target = "latestVersion", ignore = true)
    PluginRecord toRecord(Plugin entity);

    List<PluginRecord> toRecords(List<Plugin> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "developer", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalDownloads", ignore = true)
    @Mapping(target = "totalInstalls", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "versions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "installations", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateEntity(PluginRecord.Update dto, @MappingTarget Plugin entity);

    default Plugin fromId(java.util.UUID id) {
        if (id == null) return null;
        Plugin plugin = new Plugin();
        plugin.setId(id);
        return plugin;
    }
}