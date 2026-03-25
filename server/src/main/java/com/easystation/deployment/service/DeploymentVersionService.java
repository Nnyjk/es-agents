package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentVersion;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.VersionStatus;
import com.easystation.deployment.mapper.DeploymentVersionMapper;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部署版本服务
 */
@ApplicationScoped
public class DeploymentVersionService {

    /**
     * 查询版本列表
     */
    public PageResultDTO<DeploymentVersionDTO> listVersions(
            int pageNum, int pageSize, UUID applicationId, UUID environmentId,
            VersionStatus status, String keyword, String sortBy, String sortOrder) {
        
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (applicationId != null) {
            queryBuilder.append(" AND applicationId = :applicationId");
            params.put("applicationId", applicationId);
        }
        
        if (environmentId != null) {
            queryBuilder.append(" AND environmentId = :environmentId");
            params.put("environmentId", environmentId);
        }
        
        if (status != null) {
            queryBuilder.append(" AND status = :status");
            params.put("status", status);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryBuilder.append(" AND (version LIKE :keyword OR commitMessage LIKE :keyword OR notes LIKE :keyword)");
            params.put("keyword", "%" + keyword + "%");
        }
        
        Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt");
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        
        long total = DeploymentVersion.count(queryBuilder.toString(), params);
        List<DeploymentVersion> versions = DeploymentVersion.find(queryBuilder.toString(), sort, params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<DeploymentVersionDTO> result = new PageResultDTO<>();
        result.setData(DeploymentVersionMapper.toDTOList(versions));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 获取版本详情
     */
    public DeploymentVersionDTO getVersion(UUID id) {
        DeploymentVersion version = DeploymentVersion.findById(id);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + id);
        }
        return DeploymentVersionMapper.toDTO(version);
    }

    /**
     * 标记版本为稳定版本
     */
    @Transactional
    public DeploymentVersionDTO markAsStable(UUID id, String notes, String updatedBy) {
        DeploymentVersion version = DeploymentVersion.findById(id);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + id);
        }
        
        version.setIsStable(true);
        version.setIsProblematic(false);
        if (notes != null && !notes.isEmpty()) {
            version.setNotes(notes);
        }
        version.setUpdatedBy(updatedBy);
        version.setUpdatedAt(LocalDateTime.now());
        
        return DeploymentVersionMapper.toDTO(version);
    }

    /**
     * 标记版本为问题版本
     */
    @Transactional
    public DeploymentVersionDTO markAsProblematic(UUID id, String notes, String updatedBy) {
        DeploymentVersion version = DeploymentVersion.findById(id);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + id);
        }
        
        version.setIsProblematic(true);
        version.setIsStable(false);
        if (notes != null && !notes.isEmpty()) {
            version.setNotes(notes);
        }
        version.setUpdatedBy(updatedBy);
        version.setUpdatedAt(LocalDateTime.now());
        
        return DeploymentVersionMapper.toDTO(version);
    }

    /**
     * 对比两个版本
     */
    public VersionComparisonDTO compareVersions(UUID fromVersionId, UUID toVersionId) {
        DeploymentVersion fromVersion = DeploymentVersion.findById(fromVersionId);
        DeploymentVersion toVersion = DeploymentVersion.findById(toVersionId);
        
        if (fromVersion == null || toVersion == null) {
            throw new IllegalArgumentException("Version not found");
        }
        
        VersionComparisonDTO comparison = new VersionComparisonDTO();
        comparison.setFromVersion(DeploymentVersionMapper.toDTO(fromVersion));
        comparison.setToVersion(DeploymentVersionMapper.toDTO(toVersion));
        
        // TODO: 实现实际的版本对比逻辑
        // 这里需要根据实际的数据结构来对比代码变更、配置变更、依赖变更等
        List<VersionComparisonDTO.ChangeComparisonDTO> codeChanges = new ArrayList<>();
        List<VersionComparisonDTO.ChangeComparisonDTO> configChanges = new ArrayList<>();
        List<VersionComparisonDTO.ChangeComparisonDTO> dependencyChanges = new ArrayList<>();
        
        // 简单的版本号对比
        if (!fromVersion.getVersion().equals(toVersion.getVersion())) {
            VersionComparisonDTO.ChangeComparisonDTO change = new VersionComparisonDTO.ChangeComparisonDTO();
            change.setChangeType("VERSION");
            change.setKey("version");
            change.setOldValue(fromVersion.getVersion());
            change.setNewValue(toVersion.getVersion());
            change.setDescription("Version changed");
            codeChanges.add(change);
        }
        
        comparison.setCodeChanges(codeChanges);
        comparison.setConfigChanges(configChanges);
        comparison.setDependencyChanges(dependencyChanges);
        comparison.setTotalChanges(codeChanges.size() + configChanges.size() + dependencyChanges.size());
        comparison.setRiskLevel(calculateRiskLevel(comparison));
        
        return comparison;
    }

    private Integer calculateRiskLevel(VersionComparisonDTO comparison) {
        int totalChanges = comparison.getTotalChanges();
        if (totalChanges == 0) {
            return 0;
        } else if (totalChanges < 5) {
            return 1;
        } else if (totalChanges < 10) {
            return 2;
        } else if (totalChanges < 20) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * 更新版本备注
     */
    @Transactional
    public DeploymentVersionDTO updateNotes(UUID id, String notes, String updatedBy) {
        DeploymentVersion version = DeploymentVersion.findById(id);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + id);
        }
        
        version.setNotes(notes);
        version.setUpdatedBy(updatedBy);
        version.setUpdatedAt(LocalDateTime.now());
        
        return DeploymentVersionMapper.toDTO(version);
    }

    /**
     * 获取应用的版本历史
     */
    public List<DeploymentVersionDTO> getVersionHistory(UUID applicationId, UUID environmentId, int limit) {
        StringBuilder queryBuilder = new StringBuilder("applicationId = :applicationId");
        Map<String, Object> params = new HashMap<>();
        params.put("applicationId", applicationId);
        
        if (environmentId != null) {
            queryBuilder.append(" AND environmentId = :environmentId");
            params.put("environmentId", environmentId);
        }
        
        List<DeploymentVersion> versions = DeploymentVersion.find(
                queryBuilder.toString(),
                Sort.by("createdAt").descending(),
                params
        ).page(Page.of(0, limit)).list();
        
        return DeploymentVersionMapper.toDTOList(versions);
    }
}