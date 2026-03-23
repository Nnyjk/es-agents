package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.PluginReview;
import com.easystation.plugin.dto.PluginReviewRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginReviewMapper {

    @Mapping(target = "pluginId", source = "plugin.id")
    @Mapping(target = "pluginName", source = "plugin.name")
    @Mapping(target = "versionId", source = "version.id")
    @Mapping(target = "version", source = "version.version")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", source = "reviewer.username")
    PluginReviewRecord toRecord(PluginReview entity);

    List<PluginReviewRecord> toRecords(List<PluginReview> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plugin", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "securityCheckResult", ignore = true)
    @Mapping(target = "compatibilityCheckResult", ignore = true)
    @Mapping(target = "testReport", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PluginReviewRecord.Create dto, @MappingTarget PluginReview entity);

    default PluginReview fromId(java.util.UUID id) {
        if (id == null) return null;
        PluginReview review = new PluginReview();
        review.setId(id);
        return review;
    }
}