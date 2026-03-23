package com.easystation.deployment.service;

import com.easystation.deployment.domain.ApplicationConfig;
import com.easystation.deployment.dto.ApplicationConfigDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.mapper.ApplicationConfigMapper;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 应用配置服务
 */
@ApplicationScoped
public class ApplicationConfigService {

    @Inject
    ApplicationConfigMapper mapper;

    /**
     * 分页查询配置
     */
    public PageResultDTO<ApplicationConfigDTO> listConfigs(int pageNum, int pageSize, UUID applicationId, 
            UUID environmentId, ApplicationConfig.ConfigType configType) {
        StringBuilder query = new StringBuilder("applicationId = ?1");
        List<Object> params = new ArrayList<>();
        params.add(applicationId);
        
        int paramIndex = 2;
        if (environmentId != null) {
            query.append(" and environmentId = ?").append(paramIndex++);
            params.add(environmentId);
        }
        if (configType != null) {
            query.append(" and configType = ?").append(paramIndex++);
            params.add(configType);
        }
        
        long total = ApplicationConfig.count(query.toString(), params.toArray());
        List<ApplicationConfig> configs = ApplicationConfig.find(query.toString(), Sort.by("createdAt").descending(), params.toArray())
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        List<ApplicationConfigDTO> items = configs.stream()
                .map(mapper::toDTO)
                .toList();
        
        return new PageResultDTO<>(items, total, pageNum, pageSize);
    }

    /**
     * 根据ID获取配置
     */
    public ApplicationConfigDTO getById(UUID id) {
        ApplicationConfig config = ApplicationConfig.findById(id);
        return mapper.toDTO(config);
    }

    /**
     * 根据应用ID获取所有配置
     */
    public List<ApplicationConfigDTO> getByApplicationId(UUID applicationId) {
        List<ApplicationConfig> configs = ApplicationConfig.find("applicationId", applicationId).list();
        return configs.stream().map(mapper::toDTO).toList();
    }

    /**
     * 创建配置
     */
    @Transactional
    public ApplicationConfigDTO create(ApplicationConfigDTO dto) {
        ApplicationConfig config = mapper.toEntity(dto);
        config.id = UUID.randomUUID();
        config.createdAt = LocalDateTime.now();
        config.updatedAt = config.createdAt;
        config.persist();
        return mapper.toDTO(config);
    }

    /**
     * 更新配置
     */
    @Transactional
    public ApplicationConfigDTO update(UUID id, ApplicationConfigDTO dto) {
        ApplicationConfig config = ApplicationConfig.findById(id);
        if (config == null) {
            return null;
        }
        mapper.updateEntity(config, dto);
        config.updatedAt = LocalDateTime.now();
        config.persist();
        return mapper.toDTO(config);
    }

    /**
     * 删除配置
     */
    @Transactional
    public boolean delete(UUID id) {
        ApplicationConfig config = ApplicationConfig.findById(id);
        if (config == null) {
            return false;
        }
        config.delete();
        return true;
    }

    /**
     * 删除应用的所有配置
     */
    @Transactional
    public int deleteByApplication(UUID applicationId) {
        return ApplicationConfig.delete("applicationId", applicationId);
    }

    /**
     * 设置配置激活状态
     */
    @Transactional
    public ApplicationConfigDTO setActive(UUID id, boolean active) {
        ApplicationConfig config = ApplicationConfig.findById(id);
        if (config == null) {
            return null;
        }
        config.active = active;
        config.updatedAt = LocalDateTime.now();
        config.persist();
        return mapper.toDTO(config);
    }
}