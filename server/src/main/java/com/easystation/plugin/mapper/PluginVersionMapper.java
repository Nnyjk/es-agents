package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.PluginVersion;
import com.easystation.plugin.dto.PluginVersionRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginVersionMapper {

    @Mapping(target = "pluginId", source = "plugin.id")
    @Mapping(target = "pluginName", source = "plugin.name")
    @Mapping(target = "dependencies", expression = "java(entity.getDependenciesAsList())")
    PluginVersionRecord toRecord(PluginVersion entity);

    List<PluginVersionRecord> toRecords(List<PluginVersion> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plugin", ignore = true)
    @Mapping(target = "versionCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "downloadCount", ignore = true)
    @Mapping(target = "installCount", ignore = true)
    @Mapping(target = "isLatest", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PluginVersionRecord.Create dto, @MappingTarget PluginVersion entity);

    default PluginVersion fromId(java.util.UUID id) {
        if (id == null) return null;
        PluginVersion version = new PluginVersion();
        version.setId(id);
        return version;
    }
}