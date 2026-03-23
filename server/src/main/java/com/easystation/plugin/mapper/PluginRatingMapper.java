package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.PluginRating;
import com.easystation.plugin.dto.PluginRatingRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginRatingMapper {

    @Mapping(target = "pluginId", source = "plugin.id")
    @Mapping(target = "pluginName", source = "plugin.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    PluginRatingRecord toRecord(PluginRating entity);

    List<PluginRatingRecord> toRecords(List<PluginRating> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plugin", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "helpfulCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PluginRatingRecord.Create dto, @MappingTarget PluginRating entity);

    default PluginRating fromId(java.util.UUID id) {
        if (id == null) return null;
        PluginRating rating = new PluginRating();
        rating.setId(id);
        return rating;
    }
}