package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.PluginCategory;
import com.easystation.plugin.dto.PluginCategoryRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginCategoryMapper {

    @Mapping(target = "pluginCount", ignore = true)
    @Mapping(target = "children", ignore = true)
    PluginCategoryRecord toRecord(PluginCategory entity);

    List<PluginCategoryRecord> toRecords(List<PluginCategory> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "plugins", ignore = true)
    void updateEntity(PluginCategoryRecord.Update dto, @MappingTarget PluginCategory entity);

    default PluginCategory fromId(java.util.UUID id) {
        if (id == null) return null;
        PluginCategory category = new PluginCategory();
        category.setId(id);
        return category;
    }
}