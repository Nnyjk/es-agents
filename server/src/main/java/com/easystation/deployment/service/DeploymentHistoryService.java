package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentRelease;
import com.easystation.deployment.domain.DeploymentRollback;
import com.easystation.deployment.domain.DeploymentVersion;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.ReleaseStatus;
import com.easystation.deployment.enums.RollbackStatus;
import com.easystation.deployment.enums.VersionStatus;
import com.easystation.deployment.mapper.DeploymentVersionMapper;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部署历史服务 - 增强版
 * 提供部署历史记录查询、统计、导出等功能
 */
@ApplicationScoped
public class DeploymentHistoryService {

    /**
     * 查询部署历史记录
     */
    public PageResultDTO<Map<String, Object>> listHistory(
            int pageNum, int pageSize, UUID applicationId, UUID environmentId,
            String version, String status, String triggeredBy,
            LocalDateTime startTime, LocalDateTime endTime, String keyword,
            String sortBy, String sortOrder) {
        
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
        
        if (version != null && !version.isEmpty()) {
            queryBuilder.append(" AND version LIKE :version");
            params.put("version", "%" + version + "%");
        }
        
        if (status != null && !status.isEmpty()) {
            queryBuilder.append(" AND status = :status");
            params.put("status", ReleaseStatus.valueOf(status));
        }
        
        if (triggeredBy != null && !triggeredBy.isEmpty()) {
            queryBuilder.append(" AND createdBy LIKE :triggeredBy");
            params.put("triggeredBy", "%" + triggeredBy + "%");
        }
        
        if (startTime != null) {
            queryBuilder.append(" AND createdAt >= :startTime");
            params.put("startTime", startTime);
        }
        
        if (endTime != null) {
            queryBuilder.append(" AND createdAt <= :endTime");
            params.put("endTime", endTime);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryBuilder.append(" AND (version LIKE :keyword OR changeLog LIKE :keyword)");
            params.put("keyword", "%" + keyword + "%");
        }
        
        Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt");
        if ("DESC".equalsIgnoreCase(sortOrder)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        
        long total = DeploymentRelease.count(queryBuilder.toString(), params);
        List<DeploymentRelease> releases = DeploymentRelease.find(queryBuilder.toString(), sort, params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        List<Map<String, Object>> historyList = releases.stream()
                .map(this::convertToHistoryMap)
                .collect(Collectors.toList());
        
        PageResultDTO<Map<String, Object>> result = new PageResultDTO<>();
        result.setData(historyList);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    private Map<String, Object> convertToHistoryMap(DeploymentRelease release) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", release.getId());
        map.put("releaseId", release.getReleaseId());
        map.put("applicationId", release.getApplicationId());
        map.put("environmentId", release.getEnvironmentId());
        map.put("version", release.getVersion());
        map.put("releaseType", release.getType());
        map.put("releaseStatus", release.getStatus());
        map.put("triggerType", release.getType());
        map.put("triggeredBy", release.getCreatedBy());
        map.put("changeLog", release.getChangeLog());
        map.put("startedAt", release.getDeployedAt());
        map.put("finishedAt", release.getCompletedAt());
        map.put("duration", calculateDuration(release.getDeployedAt(), release.getCompletedAt()));
        map.put("createdAt", release.getCreatedAt());
        return map;
    }
    
    private Long calculateDuration(LocalDateTime startedAt, LocalDateTime finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return null;
        }
        return Duration.between(startedAt, finishedAt).getSeconds();
    }

    /**
     * 获取部署详情
     */
    public Map<String, Object> getHistoryDetail(UUID releaseId) {
        DeploymentRelease release = DeploymentRelease.findById(releaseId);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + releaseId);
        }
        
        Map<String, Object> detail = convertToHistoryMap(release);
        
        // 查找关联的版本信息
        Optional<DeploymentVersion> versionOpt = DeploymentVersion.find(
                "releaseId = ?1", releaseId
        ).firstResultOptional();
        
        if (versionOpt.isPresent()) {
            detail.put("versionDetail", DeploymentVersionMapper.toDTO(versionOpt.get()));
        }
        
        // 查找关联的回滚记录
        List<DeploymentRollback> rollbacks = DeploymentRollback.find(
                "applicationId = ?1 AND environmentId = ?2 AND fromVersion = ?3",
                release.getApplicationId(),
                release.getEnvironmentId(),
                release.getVersion()
        ).list();
        
        detail.put("relatedRollbacks", rollbacks.stream()
                .map(r -> Map.of(
                        "rollbackId", r.getRollbackId(),
                        "status", r.getStatus(),
                        "triggeredBy", r.getCreatedBy(),
                        "createdAt", r.getCreatedAt()
                ))
                .collect(Collectors.toList()));
        
        return detail;
    }

    /**
     * 获取部署统计
     */
    public DeploymentStatisticsDTO getStatistics(
            UUID applicationId, UUID environmentId,
            LocalDateTime startTime, LocalDateTime endTime) {
        
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
        
        if (startTime != null) {
            queryBuilder.append(" AND createdAt >= :startTime");
            params.put("startTime", startTime);
        }
        
        if (endTime != null) {
            queryBuilder.append(" AND createdAt <= :endTime");
            params.put("endTime", endTime);
        }
        
        List<DeploymentRelease> releases = DeploymentRelease.find(queryBuilder.toString(), params).list();
        
        DeploymentStatisticsDTO stats = new DeploymentStatisticsDTO();
        stats.setTotalDeployments((long) releases.size());
        
        long successCount = releases.stream()
                .filter(r -> r.getStatus() == ReleaseStatus.SUCCESS)
                .count();
        long failedCount = releases.stream()
                .filter(r -> r.getStatus() == ReleaseStatus.FAILED)
                .count();
        long rollbackCount = releases.stream()
                .filter(r -> r.getStatus() == ReleaseStatus.ROLLED_BACK)
                .count();
        
        stats.setSuccessCount(successCount);
        stats.setFailedCount(failedCount);
        stats.setRollbackCount(rollbackCount);
        
        if (stats.getTotalDeployments() > 0) {
            stats.setSuccessRate((double) successCount / stats.getTotalDeployments() * 100);
        } else {
            stats.setSuccessRate(0.0);
        }
        
        // 计算平均部署时长
        OptionalDouble avgDuration = releases.stream()
                .filter(r -> r.getDeployedAt() != null && r.getCompletedAt() != null)
                .mapToLong(r -> calculateDuration(r.getDeployedAt(), r.getCompletedAt()))
                .average();
        stats.setAvgDuration(avgDuration.orElse(0.0));
        
        // 失败原因分布
        Map<String, Long> failureReasons = releases.stream()
                .filter(r -> r.getStatus() == ReleaseStatus.FAILED)
                .collect(Collectors.groupingBy(
                        r -> extractFailureReason(r.getDeployProgress()),
                        Collectors.counting()
                ));
        
        List<DeploymentStatisticsDTO.FailureReasonDTO> failureReasonDTOs = failureReasons.entrySet().stream()
                .map(entry -> {
                    DeploymentStatisticsDTO.FailureReasonDTO dto = new DeploymentStatisticsDTO.FailureReasonDTO();
                    dto.setReason(entry.getKey());
                    dto.setCount(entry.getValue());
                    if (failedCount > 0) {
                        dto.setPercentage((double) entry.getValue() / failedCount * 100);
                    }
                    return dto;
                })
                .sorted(Comparator.comparingLong(DeploymentStatisticsDTO.FailureReasonDTO::getCount).reversed())
                .collect(Collectors.toList());
        
        stats.setFailureReasons(failureReasonDTOs);
        
        // 部署趋势
        Map<String, Long> trendMap = releases.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));
        
        List<DeploymentStatisticsDTO.DeploymentTrendDTO> trends = trendMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    DeploymentStatisticsDTO.DeploymentTrendDTO trend = new DeploymentStatisticsDTO.DeploymentTrendDTO();
                    trend.setDate(entry.getKey());
                    trend.setCount(entry.getValue());
                    // 计算该日期的成功率
                    long dayTotal = releases.stream()
                            .filter(r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).equals(entry.getKey()))
                            .count();
                    long daySuccess = releases.stream()
                            .filter(r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).equals(entry.getKey()))
                            .filter(r -> r.getStatus() == ReleaseStatus.SUCCESS)
                            .count();
                    if (dayTotal > 0) {
                        trend.setSuccessRate((double) daySuccess / dayTotal * 100);
                    }
                    return trend;
                })
                .collect(Collectors.toList());
        
        stats.setDeploymentTrends(trends);
        
        return stats;
    }

    private String extractFailureReason(String logs) {
        if (logs == null || logs.isEmpty()) {
            return "Unknown";
        }
        // 简单的失败原因提取逻辑
        if (logs.contains("timeout") || logs.contains("Timeout")) {
            return "Timeout";
        } else if (logs.contains("connection") || logs.contains("Connection")) {
            return "Connection Error";
        } else if (logs.contains("permission") || logs.contains("Permission")) {
            return "Permission Denied";
        } else if (logs.contains("out of memory") || logs.contains("OOM")) {
            return "Out of Memory";
        } else {
            return "Other";
        }
    }

    /**
     * 导出部署历史
     */
    public List<Map<String, Object>> exportHistory(
            UUID applicationId, UUID environmentId,
            LocalDateTime startTime, LocalDateTime endTime,
            List<String> fields) {
        
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
        
        if (startTime != null) {
            queryBuilder.append(" AND createdAt >= :startTime");
            params.put("startTime", startTime);
        }
        
        if (endTime != null) {
            queryBuilder.append(" AND createdAt <= :endTime");
            params.put("endTime", endTime);
        }
        
        List<DeploymentRelease> releases = DeploymentRelease.find(
                queryBuilder.toString(),
                Sort.by("createdAt").descending(),
                params
        ).list();
        
        return releases.stream()
                .map(release -> {
                    Map<String, Object> row = new HashMap<>();
                    if (fields == null || fields.isEmpty()) {
                        row.putAll(convertToHistoryMap(release));
                    } else {
                        for (String field : fields) {
                            switch (field) {
                                case "releaseId":
                                    row.put(field, release.getReleaseId());
                                    break;
                                case "version":
                                    row.put(field, release.getVersion());
                                    break;
                                case "status":
                                    row.put(field, release.getStatus());
                                    break;
                                case "triggeredBy":
                                    row.put(field, release.getCreatedBy());
                                    break;
                                case "startedAt":
                                    row.put(field, release.getDeployedAt());
                                    break;
                                case "finishedAt":
                                    row.put(field, release.getCompletedAt());
                                    break;
                                case "duration":
                                    row.put(field, calculateDuration(release.getDeployedAt(), release.getCompletedAt()));
                                    break;
                                case "changeLog":
                                    row.put(field, release.getChangeLog());
                                    break;
                                case "createdAt":
                                    row.put(field, release.getCreatedAt());
                                    break;
                            }
                        }
                    }
                    return row;
                })
                .collect(Collectors.toList());
    }
}