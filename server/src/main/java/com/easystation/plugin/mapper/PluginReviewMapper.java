package com.easystation.plugin.mapper;

import com.easystation.plugin.domain.PluginReview;
import com.easystation.plugin.dto.PluginReviewRecord;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginReviewMapper {

    public PluginReviewRecord toRecord(PluginReview entity) {
        if (entity == null) {
            return null;
        }
        
        return new PluginReviewRecord(
            entity.id,
            entity.pluginId,
            null, // pluginName - needs to be set by service
            entity.versionId,
            null, // version - needs to be set by service
            entity.reviewerId,
            null, // reviewerName - needs to be set by service
            entity.status,
            entity.reviewType,
            entity.comment,
            entity.securityCheckResult,
            entity.compatibilityCheckResult,
            entity.testReport,
            entity.reviewedAt,
            entity.createdAt,
            entity.updatedAt
        );
    }

    public List<PluginReviewRecord> toRecords(List<PluginReview> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toRecord)
            .collect(Collectors.toList());
    }

    public PluginReview toEntity(PluginReviewRecord.Create create, UUID reviewerId) {
        if (create == null) {
            return null;
        }
        
        PluginReview entity = new PluginReview();
        entity.pluginId = create.pluginId();
        entity.versionId = create.versionId();
        entity.reviewerId = reviewerId;
        entity.reviewType = create.reviewType();
        entity.comment = create.comment();
        entity.status = com.easystation.plugin.domain.enums.ReviewStatus.PENDING;
        
        return entity;
    }

    public void updateEntityForApprove(PluginReview entity, PluginReviewRecord.Approve approve) {
        if (entity == null || approve == null) {
            return;
        }
        
        entity.status = com.easystation.plugin.domain.enums.ReviewStatus.APPROVED;
        entity.comment = approve.comment();
        entity.securityCheckResult = approve.securityCheckResult();
        entity.compatibilityCheckResult = approve.compatibilityCheckResult();
        entity.testReport = approve.testReport();
        entity.reviewedAt = LocalDateTime.now();
    }

    public void updateEntityForReject(PluginReview entity, PluginReviewRecord.Reject reject) {
        if (entity == null || reject == null) {
            return;
        }
        
        entity.status = com.easystation.plugin.domain.enums.ReviewStatus.REJECTED;
        entity.comment = reject.comment();
        entity.reviewedAt = LocalDateTime.now();
    }
}