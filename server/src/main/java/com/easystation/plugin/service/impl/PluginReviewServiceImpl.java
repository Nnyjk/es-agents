package com.easystation.plugin.service.impl;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.domain.PluginReview;
import com.easystation.plugin.domain.PluginVersion;
import com.easystation.plugin.domain.enums.ReviewStatus;
import com.easystation.plugin.dto.PluginReviewRecord;
import com.easystation.plugin.mapper.PluginReviewMapper;
import com.easystation.plugin.repository.PluginRepository;
import com.easystation.plugin.repository.PluginReviewRepository;
import com.easystation.plugin.repository.PluginVersionRepository;
import com.easystation.plugin.service.PluginReviewService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginReviewServiceImpl implements PluginReviewService {

    @Inject
    PluginReviewRepository reviewRepository;

    @Inject
    PluginRepository pluginRepository;

    @Inject
    PluginVersionRepository versionRepository;

    @Inject
    PluginReviewMapper reviewMapper;

    @Override
    @Transactional
    public PluginReviewRecord createReview(PluginReviewRecord.Create create) {
        Plugin plugin = pluginRepository.findByIdOptional(create.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + create.pluginId()));

        // Check if there's already a pending review
        if (reviewRepository.existsByPluginIdAndStatus(create.pluginId(), ReviewStatus.PENDING)) {
            throw new BadRequestException("Plugin already has a pending review");
        }

        PluginReview review = new PluginReview();
        review.pluginId = plugin.id;

        if (create.versionId() != null) {
            PluginVersion version = versionRepository.findByIdOptional(create.versionId())
                .orElseThrow(() -> new NotFoundException("Version not found: " + create.versionId()));
            review.setVersion(version);
        }

        review.setReviewType(create.reviewType());
        review.setComment(create.comment());
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.persist(review);
        return reviewMapper.toRecord(review);
    }

    @Override
    @Transactional
    public PluginReviewRecord submit(PluginReviewRecord.Submit submit) {
        // Convert Submit to Create and delegate
        PluginReviewRecord.Create create = new PluginReviewRecord.Create(
            submit.pluginId(),
            submit.versionId(),
            submit.reviewType(),
            submit.comment()
        );
        return createReview(create);
    }

    @Override
    @Transactional
    public PluginReviewRecord approve(UUID reviewId, PluginReviewRecord.Approve approve) {
        PluginReview review = reviewRepository.findByIdOptional(reviewId)
            .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new BadRequestException("Review is not in pending status");
        }

        review.setStatus(ReviewStatus.APPROVED);
        review.setComment(approve.comment());
        review.setSecurityCheckResult(approve.securityCheckResult());
        review.setCompatibilityCheckResult(approve.compatibilityCheckResult());
        review.setTestReport(approve.testReport());
        review.setReviewedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.persist(review);

        // Update plugin status
        Plugin plugin = review.getPlugin();
        plugin.setStatus(com.easystation.plugin.domain.enums.PluginStatus.PUBLISHED);
        plugin.setPublishedAt(LocalDateTime.now());
        plugin.setUpdatedAt(LocalDateTime.now());
        pluginRepository.persist(plugin);

        return reviewMapper.toRecord(review);
    }

    @Override
    @Transactional
    public PluginReviewRecord reject(UUID reviewId, PluginReviewRecord.Reject reject) {
        PluginReview review = reviewRepository.findByIdOptional(reviewId)
            .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new BadRequestException("Review is not in pending status");
        }

        review.setStatus(ReviewStatus.REJECTED);
        review.setComment(reject.comment());
        review.setReviewedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.persist(review);

        // Update plugin status
        Plugin plugin = review.getPlugin();
        plugin.setStatus(com.easystation.plugin.domain.enums.PluginStatus.REJECTED);
        plugin.setUpdatedAt(LocalDateTime.now());
        pluginRepository.persist(plugin);

        return reviewMapper.toRecord(review);
    }

    @Override
    public Optional<PluginReviewRecord> findById(UUID id) {
        return reviewRepository.findByIdOptional(id)
            .map(reviewMapper::toRecord);
    }

    @Override
    public List<PluginReviewRecord> findByPluginId(UUID pluginId) {
        return reviewRepository.findByPluginIdOrderByCreatedAtDesc(pluginId).stream()
            .map(reviewMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginReviewRecord> findByStatus(ReviewStatus status) {
        return reviewRepository.findByStatus(status).stream()
            .map(reviewMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<PluginReviewRecord> findPendingByPluginId(UUID pluginId) {
        return reviewRepository.findPendingByPluginId(pluginId)
            .map(reviewMapper::toRecord);
    }

    @Override
    public List<PluginReviewRecord> search(PluginReviewRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.pluginId() != null) {
            queryBuilder.append(" and plugin.id = ?").append(paramIndex);
            params.add(query.pluginId());
            paramIndex++;
        }

        if (query.versionId() != null) {
            queryBuilder.append(" and version.id = ?").append(paramIndex);
            params.add(query.versionId());
            paramIndex++;
        }

        if (query.status() != null) {
            queryBuilder.append(" and status = ?").append(paramIndex);
            params.add(query.status());
            paramIndex++;
        }

        if (query.reviewType() != null && !query.reviewType().isBlank()) {
            queryBuilder.append(" and reviewType = ?").append(paramIndex);
            params.add(query.reviewType());
            paramIndex++;
        }

        queryBuilder.append(" ORDER BY createdAt DESC");

        int page = query.page() != null ? query.page() : 0;
        int size = query.size() != null ? query.size() : 20;

        return reviewRepository.find(queryBuilder.toString(), params.toArray())
            .page(Page.of(page, size))
            .list()
            .stream()
            .map(reviewMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(ReviewStatus status) {
        return reviewRepository.countByStatus(status);
    }

    @Override
    public long countByPluginId(UUID pluginId) {
        return reviewRepository.countByPluginId(pluginId);
    }
}