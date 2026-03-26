package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentRollback;
import com.easystation.deployment.domain.DeploymentVersion;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.RollbackStatus;
import com.easystation.deployment.enums.VersionStatus;
import com.easystation.deployment.mapper.DeploymentRollbackMapper;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 部署回滚服务
 */
@ApplicationScoped
public class DeploymentRollbackService {

    /**
     * 查询回滚历史
     */
    public PageResultDTO<DeploymentRollbackDTO> listRollbacks(
            int pageNum, int pageSize, UUID applicationId, UUID environmentId,
            RollbackStatus status, String sortBy, String sortOrder) {
        
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
        
        Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt");
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        
        long total = DeploymentRollback.count(queryBuilder.toString(), params);
        List<DeploymentRollback> rollbacks = DeploymentRollback.find(queryBuilder.toString(), sort, params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<DeploymentRollbackDTO> result = new PageResultDTO<>();
        result.setData(DeploymentRollbackMapper.toDTOList(rollbacks));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 获取回滚详情
     */
    public DeploymentRollbackDTO getRollback(UUID id) {
        DeploymentRollback rollback = DeploymentRollback.findById(id);
        if (rollback == null) {
            throw new IllegalArgumentException("Rollback not found: " + id);
        }
        return DeploymentRollbackMapper.toDTO(rollback);
    }

    /**
     * 回滚预检
     */
    public RollbackPrecheckDTO precheckRollback(UUID targetVersionId) {
        DeploymentVersion targetVersion = DeploymentVersion.findById(targetVersionId);
        if (targetVersion == null) {
            throw new IllegalArgumentException("Target version not found: " + targetVersionId);
        }
        
        RollbackPrecheckDTO precheck = new RollbackPrecheckDTO();
        precheck.setToVersion(targetVersion.getVersion());
        
        // 查找当前版本
        Optional<DeploymentVersion> currentVersionOpt = DeploymentVersion.find(
                "applicationId = ?1 AND environmentId = ?2 AND status = ?3",
                targetVersion.getApplicationId(),
                targetVersion.getEnvironmentId(),
                VersionStatus.CURRENT
        ).firstResultOptional();
        
        if (currentVersionOpt.isPresent()) {
            DeploymentVersion currentVersion = currentVersionOpt.get();
            precheck.setFromVersion(currentVersion.getVersion());
        }
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> checklist = new ArrayList<>();
        
        // 检查目标版本状态
        if (targetVersion.getStatus() == VersionStatus.PROBLEMATIC) {
            warnings.add("Target version is marked as problematic");
        }
        
        // 检查制品是否可用
        if (targetVersion.getArtifactUrl() == null || targetVersion.getArtifactUrl().isEmpty()) {
            errors.add("Target version artifact is not available");
        }
        
        // 检查配置是否存在
        if (targetVersion.getConfig() == null || targetVersion.getConfig().isEmpty()) {
            warnings.add("Target version has no configuration saved");
        }
        
        // 执行前置检查列表
        checklist.add("Check target version status");
        checklist.add("Verify artifact availability");
        checklist.add("Check configuration completeness");
        checklist.add("Verify environment compatibility");
        
        precheck.setWarnings(warnings);
        precheck.setErrors(errors);
        precheck.setChecklist(checklist);
        precheck.setCanRollback(errors.isEmpty());
        
        // 影响分析
        RollbackPrecheckDTO.ImpactAnalysisDTO impactAnalysis = new RollbackPrecheckDTO.ImpactAnalysisDTO();
        impactAnalysis.setAffectedServices(1);
        impactAnalysis.setAffectedInstances(1);
        impactAnalysis.setAffectedComponents(Arrays.asList("Application", "Configuration"));
        impactAnalysis.setEstimatedTime("2-5 minutes");
        impactAnalysis.setRiskLevel(warnings.isEmpty() ? "LOW" : "MEDIUM");
        precheck.setImpactAnalysis(impactAnalysis);
        
        return precheck;
    }

    /**
     * 执行回滚
     */
    @Transactional
    public DeploymentRollbackDTO executeRollback(
            UUID applicationId, UUID environmentId, UUID fromVersionId, UUID toVersionId,
            RollbackRequestDTO request, String triggeredBy) {
        
        DeploymentVersion fromVersion = DeploymentVersion.findById(fromVersionId);
        DeploymentVersion toVersion = DeploymentVersion.findById(toVersionId);
        
        if (fromVersion == null || toVersion == null) {
            throw new IllegalArgumentException("Version not found");
        }
        
        // 创建回滚记录
        DeploymentRollback rollback = new DeploymentRollback();
        rollback.setRollbackId("RB-" + System.currentTimeMillis());
        rollback.setApplicationId(applicationId);
        rollback.setEnvironmentId(environmentId);
        rollback.setFromVersionId(fromVersionId);
        rollback.setToVersionId(toVersionId);
        rollback.setFromVersion(fromVersion.getVersion());
        rollback.setToVersion(toVersion.getVersion());
        rollback.setStrategy(request.getStrategy());
        rollback.setStatus(RollbackStatus.PENDING);
        rollback.setReason(request.getReason());
        rollback.setTimeoutConfig(request.getTimeout());
        rollback.setMaxRetry(request.getMaxRetry());
        rollback.setNotifyConfig(request.getNotifyConfig());
        rollback.setTriggeredBy(triggeredBy);
        rollback.setTriggeredAt(LocalDateTime.now());
        rollback.setCreatedBy(triggeredBy);
        
        rollback.persist();
        
        // TODO: 实际执行回滚逻辑
        // 这里需要调用实际的部署服务来执行回滚
        // 当前是模拟实现
        
        return DeploymentRollbackMapper.toDTO(rollback);
    }

    /**
     * 取消回滚
     */
    @Transactional
    public DeploymentRollbackDTO cancelRollback(UUID id) {
        DeploymentRollback rollback = DeploymentRollback.findById(id);
        if (rollback == null) {
            throw new IllegalArgumentException("Rollback not found: " + id);
        }
        
        if (rollback.getStatus() == RollbackStatus.EXECUTING || 
            rollback.getStatus() == RollbackStatus.VERIFYING) {
            throw new IllegalStateException("Cannot cancel rollback in progress");
        }
        
        rollback.setStatus(RollbackStatus.CANCELLED);
        rollback.setUpdatedAt(LocalDateTime.now());
        
        return DeploymentRollbackMapper.toDTO(rollback);
    }

    /**
     * 更新回滚状态
     */
    @Transactional
    public DeploymentRollbackDTO updateRollbackStatus(UUID id, RollbackStatus status, String logs) {
        DeploymentRollback rollback = DeploymentRollback.findById(id);
        if (rollback == null) {
            throw new IllegalArgumentException("Rollback not found: " + id);
        }
        
        rollback.setStatus(status);
        if (logs != null && !logs.isEmpty()) {
            String existingLogs = rollback.getLogs() != null ? rollback.getLogs() : "";
            rollback.setLogs(existingLogs + "\n" + logs);
        }
        
        if (status == RollbackStatus.SUCCESS || status == RollbackStatus.FAILED) {
            rollback.setCompletedAt(LocalDateTime.now());
            if (rollback.getStartedAt() != null) {
                rollback.setDuration(
                    java.time.Duration.between(rollback.getStartedAt(), rollback.getCompletedAt()).getSeconds()
                );
            }
        }
        
        return DeploymentRollbackMapper.toDTO(rollback);
    }
}