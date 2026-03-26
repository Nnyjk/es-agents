package com.easystation.deployment.service;

import com.easystation.deployment.domain.ApplicationDependency;
import com.easystation.deployment.dto.ApplicationDependencyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.mapper.ApplicationDependencyMapper;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 应用依赖服务
 */
@ApplicationScoped
public class ApplicationDependencyService {

    @Inject
    ApplicationDependencyMapper mapper;

    /**
     * 分页查询依赖
     */
    public PageResultDTO<ApplicationDependencyDTO> listDependencies(int pageNum, int pageSize, UUID applicationId, 
            UUID environmentId, ApplicationDependency.DependencyType type) {
        StringBuilder query = new StringBuilder("applicationId = ?1");
        List<Object> params = new ArrayList<>();
        params.add(applicationId);
        
        int paramIndex = 2;
        if (environmentId != null) {
            query.append(" and environmentId = ?").append(paramIndex++);
            params.add(environmentId);
        }
        if (type != null) {
            query.append(" and type = ?").append(paramIndex++);
            params.add(type);
        }
        
        long total = ApplicationDependency.count(query.toString(), params.toArray());
        List<ApplicationDependency> deps = ApplicationDependency.find(query.toString(), Sort.by("createdAt").descending(), params.toArray())
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        List<ApplicationDependencyDTO> items = deps.stream()
                .map(mapper::toDTO)
                .toList();
        
        PageResultDTO<ApplicationDependencyDTO> result = new PageResultDTO<>();
        result.setData(items);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 根据ID获取依赖
     */
    public ApplicationDependencyDTO getById(UUID id) {
        ApplicationDependency dep = ApplicationDependency.findById(id);
        return mapper.toDTO(dep);
    }

    /**
     * 根据应用ID获取所有依赖
     */
    public List<ApplicationDependencyDTO> getByApplicationId(UUID applicationId) {
        List<ApplicationDependency> deps = ApplicationDependency.find("applicationId", applicationId).list();
        return deps.stream().map(mapper::toDTO).toList();
    }

    /**
     * 创建依赖
     */
    @Transactional
    public ApplicationDependencyDTO create(ApplicationDependencyDTO dto) {
        ApplicationDependency dep = mapper.toEntity(dto);
        dep.id = UUID.randomUUID();
        dep.createdAt = LocalDateTime.now();
        dep.updatedAt = dep.createdAt;
        dep.persist();
        return mapper.toDTO(dep);
    }

    /**
     * 更新依赖
     */
    @Transactional
    public ApplicationDependencyDTO update(UUID id, ApplicationDependencyDTO dto) {
        ApplicationDependency dep = ApplicationDependency.findById(id);
        if (dep == null) {
            return null;
        }
        mapper.updateEntity(dep, dto);
        dep.updatedAt = LocalDateTime.now();
        dep.persist();
        return mapper.toDTO(dep);
    }

    /**
     * 删除依赖
     */
    @Transactional
    public boolean delete(UUID id) {
        ApplicationDependency dep = ApplicationDependency.findById(id);
        if (dep == null) {
            return false;
        }
        dep.delete();
        return true;
    }

    /**
     * 删除应用的所有依赖
     */
    @Transactional
    public long deleteByApplication(UUID applicationId) {
        return ApplicationDependency.delete("applicationId", applicationId);
    }

    /**
     * 设置依赖激活状态
     */
    @Transactional
    public ApplicationDependencyDTO setActive(UUID id, boolean active) {
        ApplicationDependency dep = ApplicationDependency.findById(id);
        if (dep == null) {
            return null;
        }
        dep.active = active;
        dep.updatedAt = LocalDateTime.now();
        dep.persist();
        return mapper.toDTO(dep);
    }
}