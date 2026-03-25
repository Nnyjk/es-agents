package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentChangeRecord;
import com.easystation.deployment.dto.DeploymentChangeDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.enums.ChangeType;
import com.easystation.deployment.mapper.DeploymentChangeMapper;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 部署变更服务
 */
@ApplicationScoped
public class DeploymentChangeService {

    /**
     * 查询变更记录
     */
    public PageResultDTO<DeploymentChangeDTO> listChanges(
            int pageNum, int pageSize, UUID versionId, UUID applicationId,
            ChangeType changeType, String sortBy, String sortOrder) {
        
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (versionId != null) {
            queryBuilder.append(" AND versionId = :versionId");
            params.put("versionId", versionId);
        }
        
        if (applicationId != null) {
            queryBuilder.append(" AND applicationId = :applicationId");
            params.put("applicationId", applicationId);
        }
        
        if (changeType != null) {
            queryBuilder.append(" AND changeType = :changeType");
            params.put("changeType", changeType);
        }
        
        Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt");
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        
        long total = DeploymentChangeRecord.count(queryBuilder.toString(), params);
        List<DeploymentChangeRecord> changes = DeploymentChangeRecord.find(queryBuilder.toString(), sort, params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<DeploymentChangeDTO> result = new PageResultDTO<>();
        result.setData(DeploymentChangeMapper.toDTOList(changes));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 获取变更详情
     */
    public DeploymentChangeDTO getChange(UUID id) {
        DeploymentChangeRecord change = DeploymentChangeRecord.findById(id);
        if (change == null) {
            throw new IllegalArgumentException("Change not found: " + id);
        }
        return DeploymentChangeMapper.toDTO(change);
    }

    /**
     * 创建变更记录
     */
    @Transactional
    public DeploymentChangeDTO createChange(DeploymentChangeDTO dto, String createdBy) {
        DeploymentChangeRecord change = new DeploymentChangeRecord();
        change.setVersionId(dto.getVersionId());
        change.setApplicationId(dto.getApplicationId());
        change.setChangeType(dto.getChangeType());
        change.setChangeKey(dto.getChangeKey());
        change.setChangeTitle(dto.getChangeTitle());
        change.setOldValue(dto.getOldValue());
        change.setNewValue(dto.getNewValue());
        change.setChangeDiff(dto.getChangeDiff());
        change.setDescription(dto.getDescription());
        change.setImpactLevel(dto.getImpactLevel() != null ? dto.getImpactLevel() : 1);
        change.setImpactScope(dto.getImpactScope());
        change.setRiskLevel(dto.getRiskLevel() != null ? dto.getRiskLevel() : 1);
        change.setRiskDescription(dto.getRiskDescription());
        change.setCommitHash(dto.getCommitHash());
        change.setCommitUrl(dto.getCommitUrl());
        change.setAuthor(dto.getAuthor());
        change.setAuthorEmail(dto.getAuthorEmail());
        change.setRelatedIssue(dto.getRelatedIssue());
        change.setRelatedPr(dto.getRelatedPr());
        change.setCreatedBy(createdBy);
        
        change.persist();
        return DeploymentChangeMapper.toDTO(change);
    }

    /**
     * 获取版本的变更记录
     */
    public List<DeploymentChangeDTO> getVersionChanges(UUID versionId) {
        List<DeploymentChangeRecord> changes = DeploymentChangeRecord.find(
                "versionId = ?1 ORDER BY createdAt DESC",
                versionId
        ).list();
        return DeploymentChangeMapper.toDTOList(changes);
    }

    /**
     * 分析变更影响
     */
    public Map<String, Object> analyzeImpact(UUID versionId) {
        List<DeploymentChangeRecord> changes = DeploymentChangeRecord.find(
                "versionId = ?1",
                versionId
        ).list();
        
        Map<String, Object> result = new HashMap<>();
        result.put("versionId", versionId);
        result.put("totalChanges", changes.size());
        
        // 按类型统计
        Map<ChangeType, Long> changesByType = new HashMap<>();
        for (ChangeType type : ChangeType.values()) {
            changesByType.put(type, changes.stream()
                    .filter(c -> c.getChangeType() == type)
                    .count());
        }
        result.put("changesByType", changesByType);
        
        // 按风险等级统计
        Map<Integer, Long> changesByRiskLevel = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int level = i;
            changesByRiskLevel.put(level, changes.stream()
                    .filter(c -> c.getRiskLevel() == level)
                    .count());
        }
        result.put("changesByRiskLevel", changesByRiskLevel);
        
        // 受影响的组件
        Set<String> affectedComponents = new HashSet<>();
        for (DeploymentChangeRecord change : changes) {
            if (change.getChangeKey() != null) {
                affectedComponents.add(change.getChangeKey().split("\\.")[0]);
            }
        }
        result.put("affectedComponents", affectedComponents);
        
        // 风险评估
        int highRiskChanges = (int) changes.stream()
                .filter(c -> c.getRiskLevel() >= 4)
                .count();
        result.put("highRiskChanges", highRiskChanges);
        result.put("overallRiskLevel", highRiskChanges > 5 ? "HIGH" : 
                       highRiskChanges > 2 ? "MEDIUM" : "LOW");
        
        return result;
    }

    /**
     * 批量创建变更记录
     */
    @Transactional
    public List<DeploymentChangeDTO> batchCreateChanges(List<DeploymentChangeDTO> dtos, String createdBy) {
        List<DeploymentChangeDTO> results = new ArrayList<>();
        for (DeploymentChangeDTO dto : dtos) {
            results.add(createChange(dto, createdBy));
        }
        return results;
    }
}