package com.easystation.deployment.service;

import com.easystation.deployment.domain.ArtifactRepository;
import com.easystation.deployment.dto.ArtifactRepositoryDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.mapper.ArtifactRepositoryMapper;
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
 * 制品仓库服务
 */
@ApplicationScoped
public class ArtifactRepositoryService {

    @Inject
    ArtifactRepositoryMapper mapper;

    /**
     * 分页查询仓库
     */
    public PageResultDTO<ArtifactRepositoryDTO> listRepositories(int pageNum, int pageSize, 
            ArtifactRepository.RepositoryType type, Boolean active, String keyword) {
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        
        int paramIndex = 1;
        if (type != null) {
            query.append(" and type = ?").append(paramIndex++);
            params.add(type);
        }
        if (active != null) {
            query.append(" and active = ?").append(paramIndex++);
            params.add(active);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.append(" and (name like ?").append(paramIndex).append(" or description like ?").append(paramIndex).append(")");
            params.add("%" + keyword + "%");
            paramIndex++;
        }
        
        long total = ArtifactRepository.count(query.toString(), params.toArray());
        List<ArtifactRepository> repos = ArtifactRepository.find(query.toString(), Sort.by("createdAt").descending(), params.toArray())
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        List<ArtifactRepositoryDTO> items = repos.stream()
                .map(mapper::toDTO)
                .toList();
        
        return new PageResultDTO<>(items, total, pageNum, pageSize);
    }

    /**
     * 根据ID获取仓库
     */
    public ArtifactRepositoryDTO getById(UUID id) {
        ArtifactRepository repo = ArtifactRepository.findById(id);
        return mapper.toDTO(repo);
    }

    /**
     * 根据名称获取仓库
     */
    public ArtifactRepositoryDTO getByName(String name) {
        ArtifactRepository repo = ArtifactRepository.find("name", name).firstResult();
        return mapper.toDTO(repo);
    }

    /**
     * 根据类型获取仓库列表
     */
    public List<ArtifactRepositoryDTO> getByType(ArtifactRepository.RepositoryType type) {
        List<ArtifactRepository> repos = ArtifactRepository.find("type", type).list();
        return repos.stream().map(mapper::toDTO).toList();
    }

    /**
     * 获取指定类型的默认仓库
     */
    public ArtifactRepositoryDTO getDefaultRepository(ArtifactRepository.RepositoryType type) {
        ArtifactRepository repo = ArtifactRepository.find("type = ?1 and isDefault = true and active = true", type)
                .firstResult();
        return mapper.toDTO(repo);
    }

    /**
     * 创建仓库
     */
    @Transactional
    public ArtifactRepositoryDTO create(ArtifactRepositoryDTO dto) {
        // 检查名称是否已存在
        ArtifactRepository existing = ArtifactRepository.find("name", dto.name).firstResult();
        if (existing != null) {
            throw new IllegalArgumentException("Repository name already exists: " + dto.name);
        }
        
        ArtifactRepository repo = mapper.toEntity(dto);
        repo.id = UUID.randomUUID();
        repo.createdAt = LocalDateTime.now();
        repo.updatedAt = repo.createdAt;
        repo.persist();
        return mapper.toDTO(repo);
    }

    /**
     * 更新仓库
     */
    @Transactional
    public ArtifactRepositoryDTO update(UUID id, ArtifactRepositoryDTO dto) {
        ArtifactRepository repo = ArtifactRepository.findById(id);
        if (repo == null) {
            return null;
        }
        
        // 检查名称是否与其他仓库冲突
        if (dto.name != null && !dto.name.equals(repo.name)) {
            ArtifactRepository existing = ArtifactRepository.find("name = ?1 and id != ?2", dto.name, id).firstResult();
            if (existing != null) {
                throw new IllegalArgumentException("Repository name already exists: " + dto.name);
            }
        }
        
        mapper.updateEntity(repo, dto);
        repo.updatedAt = LocalDateTime.now();
        repo.persist();
        return mapper.toDTO(repo);
    }

    /**
     * 删除仓库
     */
    @Transactional
    public boolean delete(UUID id) {
        ArtifactRepository repo = ArtifactRepository.findById(id);
        if (repo == null) {
            return false;
        }
        repo.delete();
        return true;
    }

    /**
     * 设置仓库激活状态
     */
    @Transactional
    public ArtifactRepositoryDTO setActive(UUID id, boolean active) {
        ArtifactRepository repo = ArtifactRepository.findById(id);
        if (repo == null) {
            return null;
        }
        repo.active = active;
        repo.updatedAt = LocalDateTime.now();
        repo.persist();
        return mapper.toDTO(repo);
    }

    /**
     * 设置默认仓库
     */
    @Transactional
    public ArtifactRepositoryDTO setDefault(UUID id) {
        ArtifactRepository repo = ArtifactRepository.findById(id);
        if (repo == null) {
            return null;
        }
        
        // 清除同类型的其他默认设置
        ArtifactRepository.update("isDefault = false where type = ?1 and id != ?2", repo.type, id);
        
        repo.isDefault = true;
        repo.updatedAt = LocalDateTime.now();
        repo.persist();
        return mapper.toDTO(repo);
    }
}