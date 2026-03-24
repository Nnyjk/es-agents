package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginReviewRecord;
import com.easystation.plugin.domain.enums.ReviewStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginReviewService {

    PluginReviewRecord createReview(PluginReviewRecord.Create create);

    PluginReviewRecord submit(PluginReviewRecord.Submit submit);

    PluginReviewRecord approve(UUID reviewId, PluginReviewRecord.Approve approve);

    PluginReviewRecord reject(UUID reviewId, PluginReviewRecord.Reject reject);

    Optional<PluginReviewRecord> findById(UUID id);

    List<PluginReviewRecord> findByPluginId(UUID pluginId);

    List<PluginReviewRecord> findByStatus(ReviewStatus status);

    Optional<PluginReviewRecord> findPendingByPluginId(UUID pluginId);

    List<PluginReviewRecord> search(PluginReviewRecord.Query query);

    long countByStatus(ReviewStatus status);

    long countByPluginId(UUID pluginId);
}