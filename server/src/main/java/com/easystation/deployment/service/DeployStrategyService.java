package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeployStrategy;
import com.easystation.deployment.dto.DeployStrategyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.mapper.DeployStrategyMapper;
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
 * 部署策略服务
 */
@ApplicationScoped
public class DeployStrategyService {

    @Inject
    DeployStrategyMapper mapper;

    /**
     * 分页查询策略
     */
    public PageResultDTO<DeployStrategyDTO> listStrategies(int pageNum, int pageSize, UUID applicationId, 
            UUID environmentId, DeployStrategy.StrategyType type) {
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
        
        long total = DeployStrategy.count(query.toString(), params.toArray());
        List<DeployStrategy> strategies = DeployStrategy.find(query.toString(), Sort.by("createdAt").descending(), params.toArray())
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        List<DeployStrategyDTO> items = strategies.stream()
                .map(mapper::toDTO)
                .toList();
        
        PageResultDTO<DeployStrategyDTO> result = new PageResultDTO<>();
        result.setList(items);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 根据ID获取策略
     */
    public DeployStrategyDTO getById(UUID id) {
        DeployStrategy strategy = DeployStrategy.findById(id);
        return mapper.toDTO(strategy);
    }

    /**
     * 根据应用ID获取所有策略
     */
    public List<DeployStrategyDTO> getByApplicationId(UUID applicationId) {
        List<DeployStrategy> strategies = DeployStrategy.find("applicationId", applicationId).list();
        return strategies.stream().map(mapper::toDTO).toList();
    }

    /**
     * 获取应用的默认策略
     */
    public DeployStrategyDTO getDefaultStrategy(UUID applicationId, UUID environmentId) {
        StringBuilder query = new StringBuilder("applicationId = ?1 and isDefault = true and active = true");
        List<Object> params = new ArrayList<>();
        params.add(applicationId);
        
        if (environmentId != null) {
            query.append(" and environmentId = ?2");
            params.add(environmentId);
        }
        
        DeployStrategy strategy = DeployStrategy.find(query.toString(), params.toArray()).firstResult();
        return mapper.toDTO(strategy);
    }

    /**
     * 创建策略
     */
    @Transactional
    public DeployStrategyDTO create(DeployStrategyDTO dto) {
        DeployStrategy strategy = mapper.toEntity(dto);
        strategy.id = UUID.randomUUID();
        strategy.createdAt = LocalDateTime.now();
        strategy.updatedAt = strategy.createdAt;
        strategy.persist();
        return mapper.toDTO(strategy);
    }

    /**
     * 更新策略
     */
    @Transactional
    public DeployStrategyDTO update(UUID id, DeployStrategyDTO dto) {
        DeployStrategy strategy = DeployStrategy.findById(id);
        if (strategy == null) {
            return null;
        }
        mapper.updateEntity(strategy, dto);
        strategy.updatedAt = LocalDateTime.now();
        strategy.persist();
        return mapper.toDTO(strategy);
    }

    /**
     * 删除策略
     */
    @Transactional
    public boolean delete(UUID id) {
        DeployStrategy strategy = DeployStrategy.findById(id);
        if (strategy == null) {
            return false;
        }
        strategy.delete();
        return true;
    }

    /**
     * 删除应用的所有策略
     */
    @Transactional
    public long deleteByApplication(UUID applicationId) {
        return DeployStrategy.delete("applicationId", applicationId);
    }

    /**
     * 设置策略激活状态
     */
    @Transactional
    public DeployStrategyDTO setActive(UUID id, boolean active) {
        DeployStrategy strategy = DeployStrategy.findById(id);
        if (strategy == null) {
            return null;
        }
        strategy.active = active;
        strategy.updatedAt = LocalDateTime.now();
        strategy.persist();
        return mapper.toDTO(strategy);
    }

    /**
     * 设置默认策略
     */
    @Transactional
    public DeployStrategyDTO setDefault(UUID id) {
        DeployStrategy strategy = DeployStrategy.findById(id);
        if (strategy == null) {
            return null;
        }
        
        // 清除同应用同环境的其他默认设置
        String query = "applicationId = ?1 and id != ?2";
        List<Object> params = new ArrayList<>();
        params.add(strategy.applicationId);
        params.add(id);
        
        if (strategy.environmentId != null) {
            query += " and environmentId = ?3";
            params.add(strategy.environmentId);
        }
        
        DeployStrategy.update("isDefault = false where " + query, params.toArray());
        
        strategy.isDefault = true;
        strategy.updatedAt = LocalDateTime.now();
        strategy.persist();
        return mapper.toDTO(strategy);
    }
}