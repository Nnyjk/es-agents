package com.easystation.profile.mapper;

import com.easystation.profile.dto.PreferenceRecord;
import com.easystation.profile.domain.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PreferenceMapper {

    PreferenceRecord toRecord(UserPreference entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PreferenceRecord.Update dto, @MappingTarget UserPreference entity);
}