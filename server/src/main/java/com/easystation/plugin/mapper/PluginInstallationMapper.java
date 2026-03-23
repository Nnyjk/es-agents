package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.entity.PluginInstallation;
import com.easystation.plugin.dto.PluginInstallationRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "jakarta-cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PluginInstallationMapper {

    @Mapping(target = "pluginId", source = "plugin.id")
    @Mapping(target = "pluginName", source = "plugin.name")
    @Mapping(target = "versionId", source = "version.id")
    @Mapping(target = "installedVersion", source = "version.version")
    @Mapping(target = "agentId", source = "agent.id")
    @Mapping(target = "agentName", source = "agent.name")
    @Mapping(target = "userId", source = "user.id")
    PluginInstallationRecord toRecord(PluginInstallation entity);

    List<PluginInstallationRecord> toRecords(List<PluginInstallation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "plugin", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "installPath", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "lastStartedAt", ignore = true)
    @Mapping(target = "lastStoppedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PluginInstallationRecord.Install dto, @MappingTarget PluginInstallation entity);

    default PluginInstallation fromId(java.util.UUID id) {
        if (id == null) return null;
        PluginInstallation installation = new PluginInstallation();
        installation.setId(id);
        return installation;
    }
}