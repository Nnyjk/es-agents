package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentApplication;
import com.easystation.deployment.domain.DeploymentEnvironment;
import com.easystation.deployment.domain.DeploymentRelease;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.ReleaseStatus;
import com.easystation.deployment.enums.ReleaseType;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReleaseService {

    public PageResultDTO<ReleaseDTO> listReleases(int pageNum, int pageSize, String releaseId, UUID applicationId, 
                                                   UUID environmentId, ReleaseStatus status) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        
        if (releaseId != null && !releaseId.isEmpty()) {
            queryBuilder.append(" and releaseId like :releaseId");
            params.put("releaseId", "%" + releaseId + "%");
        }
        if (applicationId != null) {
            queryBuilder.append(" and applicationId = :applicationId");
            params.put("applicationId", applicationId);
        }
        if (environmentId != null) {
            queryBuilder.append(" and environmentId = :environmentId");
            params.put("environmentId", environmentId);
        }
        if (status != null) {
            queryBuilder.append(" and status = :status");
            params.put("status", status);
        }
        
        long total = DeploymentRelease.count(queryBuilder.toString(), params);
        List<DeploymentRelease> releases = DeploymentRelease.find(queryBuilder.toString(), Sort.by("createdAt").descending(), params)
                .page(Page.of(pageNum - 1, pageSize))
                .list();
        
        PageResultDTO<ReleaseDTO> result = new PageResultDTO<>();
        result.setData(releases.stream().map(this::toDTO).collect(Collectors.toList()));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    public ReleaseDTO getRelease(UUID id) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO createRelease(ReleaseDTO dto) {
        DeploymentRelease release = new DeploymentRelease();
        release.releaseId = generateReleaseId();
        release.applicationId = dto.getApplicationId();
        release.environmentId = dto.getEnvironmentId();
        release.version = dto.getVersion();
        release.type = dto.getType() != null ? dto.getType() : ReleaseType.PATCH;
        release.status = ReleaseStatus.DRAFT;
        release.changeLog = dto.getChangeLog();
        release.createdBy = dto.getCreatedBy();
        
        release.persist();
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO submitForApproval(UUID id, String submittedBy) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        if (release.status != ReleaseStatus.DRAFT) {
            throw new IllegalStateException("Only draft releases can be submitted for approval");
        }
        
        release.status = ReleaseStatus.PENDING;
        release.persist();
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO approveRelease(UUID id, String approvedBy) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        if (release.status != ReleaseStatus.PENDING) {
            throw new IllegalStateException("Only pending releases can be approved");
        }
        
        release.status = ReleaseStatus.APPROVED;
        release.approvedBy = approvedBy;
        release.approvedAt = LocalDateTime.now();
        release.persist();
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO rejectRelease(UUID id, String rejectedBy, String reason) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        if (release.status != ReleaseStatus.PENDING) {
            throw new IllegalStateException("Only pending releases can be rejected");
        }
        
        release.status = ReleaseStatus.DRAFT;
        release.persist();
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO startRelease(UUID id, String deployedBy) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        if (release.status != ReleaseStatus.APPROVED) {
            throw new IllegalStateException("Only approved releases can be deployed");
        }
        
        release.status = ReleaseStatus.DEPLOYING;
        release.deployedBy = deployedBy;
        release.deployedAt = LocalDateTime.now();
        release.persist();
        return toDTO(release);
    }

    @Transactional
    public ReleaseDTO rollbackRelease(UUID id, String rolledBackBy) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        if (release.status != ReleaseStatus.SUCCESS) {
            throw new IllegalStateException("Only successful releases can be rolled back");
        }
        
        release.status = ReleaseStatus.ROLLED_BACK;
        release.persist();
        return toDTO(release);
    }

    public List<ReleaseDTO> getReleaseHistory(UUID applicationId) {
        List<DeploymentRelease> releases = DeploymentRelease.find("applicationId", Sort.by("createdAt").descending(), applicationId).list();
        return releases.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ReleaseDTO getReleaseDetail(UUID id) {
        DeploymentRelease release = DeploymentRelease.findById(id);
        if (release == null) {
            throw new IllegalArgumentException("Release not found: " + id);
        }
        ReleaseDTO dto = toDTO(release);
        // Add stages info placeholder
        dto.setStages(new ArrayList<>());
        return dto;
    }

    private String generateReleaseId() {
        return "REL-" + System.currentTimeMillis();
    }

    private ReleaseDTO toDTO(DeploymentRelease release) {
        ReleaseDTO dto = new ReleaseDTO();
        dto.setId(release.id);
        dto.setReleaseId(release.releaseId);
        dto.setApplicationId(release.applicationId);
        
        // Get application name
        DeploymentApplication app = DeploymentApplication.findById(release.applicationId);
        dto.setApplicationName(app != null ? app.name : null);
        
        dto.setEnvironmentId(release.environmentId);
        
        // Get environment name
        DeploymentEnvironment env = DeploymentEnvironment.findById(release.environmentId);
        dto.setEnvironmentName(env != null ? env.name : null);
        
        dto.setVersion(release.version);
        dto.setType(release.type);
        dto.setStatus(release.status);
        dto.setChangeLog(release.changeLog);
        dto.setCreatedBy(release.createdBy);
        dto.setApprovedBy(release.approvedBy);
        dto.setApprovedAt(release.approvedAt);
        dto.setDeployedBy(release.deployedBy);
        dto.setDeployedAt(release.deployedAt);
        dto.setCreatedAt(release.createdAt);
        dto.setUpdatedAt(release.updatedAt);
        
        return dto;
    }
}