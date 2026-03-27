package com.easystation.plugin.service.impl;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.domain.PluginRating;
import com.easystation.plugin.dto.PluginRatingRecord;
import com.easystation.plugin.mapper.PluginRatingMapper;
import com.easystation.plugin.repository.PluginRatingRepository;
import com.easystation.plugin.repository.PluginRepository;
import com.easystation.plugin.service.PluginRatingService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginRatingServiceImpl implements PluginRatingService {

    @Inject
    PluginRatingRepository ratingRepository;

    @Inject
    PluginRepository pluginRepository;

    @Inject
    PluginRatingMapper ratingMapper;

    @Override
    @Transactional
    public PluginRatingRecord create(PluginRatingRecord.Create create, UUID userId) {
        Plugin plugin = pluginRepository.findByIdOptional(create.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + create.pluginId()));

        // Check if user has already rated this plugin
        if (ratingRepository.existsByPluginIdAndUserId(create.pluginId(), userId)) {
            throw new BadRequestException("User has already rated this plugin");
        }

        PluginRating rating = new PluginRating();
        rating.setPluginId(plugin.id);
        rating.setUserId(userId);
        rating.setRating(create.rating());
        rating.setReview(create.review());
        rating.setIsVerified(false);
        rating.setHelpfulCount(0);
        rating.setCreatedAt(LocalDateTime.now());
        rating.setUpdatedAt(LocalDateTime.now());

        ratingRepository.persist(rating);

        // Update plugin rating statistics
        updatePluginRatingStatistics(create.pluginId());

        return ratingMapper.toRecord(rating);
    }

    @Override
    @Transactional
    public PluginRatingRecord update(UUID id, PluginRatingRecord.Update update) {
        PluginRating rating = ratingRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Rating not found: " + id));

        if (update.rating() != null) {
            rating.setRating(update.rating());
        }
        if (update.review() != null) {
            rating.setReview(update.review());
        }
        rating.setUpdatedAt(LocalDateTime.now());

        ratingRepository.persist(rating);

        // Update plugin rating statistics
        updatePluginRatingStatistics(rating.getPluginId());

        return ratingMapper.toRecord(rating);
    }

    @Override
    public Optional<PluginRatingRecord> findById(UUID id) {
        return ratingRepository.findByIdOptional(id)
            .map(ratingMapper::toRecord);
    }

    @Override
    public Optional<PluginRatingRecord> findByPluginIdAndUserId(UUID pluginId, UUID userId) {
        return ratingRepository.findByPluginIdAndUserId(pluginId, userId)
            .map(ratingMapper::toRecord);
    }

    @Override
    public List<PluginRatingRecord> findByPluginId(UUID pluginId) {
        return ratingRepository.findByPluginIdOrderByCreatedAtDesc(pluginId).stream()
            .map(ratingMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginRatingRecord> findByUserId(UUID userId) {
        return ratingRepository.findByUserId(userId).stream()
            .map(ratingMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginRatingRecord> search(PluginRatingRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.getPluginId() != null) {
            queryBuilder.append(" and plugin.id = ?").append(paramIndex);
            params.add(query.getPluginId());
            paramIndex++;
        }

        if (query.getUserId() != null) {
            queryBuilder.append(" and user.id = ?").append(paramIndex);
            params.add(query.getUserId());
            paramIndex++;
        }

        if (query.getVerified() != null) {
            queryBuilder.append(" and isVerified = ?").append(paramIndex);
            params.add(query.getVerified());
            paramIndex++;
        }

        String sortField = query.getSortBy() != null ? query.getSortBy() : "createdAt";
        String sortOrder = "DESC".equalsIgnoreCase(query.getSortOrder()) ? "DESC" : "ASC";
        queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);

        int page = query.getPage() != null ? query.getPage() : 0;
        int size = query.getSize() != null ? query.getSize() : 20;

        return ratingRepository.find(queryBuilder.toString(), params.toArray())
            .page(Page.of(page, size))
            .list()
            .stream()
            .map(ratingMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public PluginRatingRecord.Summary getSummary(UUID pluginId) {
        BigDecimal averageRating = getAverageRating(pluginId);
        int totalCount = (int) ratingRepository.countByPluginId(pluginId);
        int[] distribution = getRatingDistribution(pluginId);

        return new PluginRatingRecord.Summary(averageRating, totalCount, distribution);
    }

    @Override
    public BigDecimal getAverageRating(UUID pluginId) {
        return ratingRepository.calculateAverageRatingByPluginId(pluginId)
            .setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public int[] getRatingDistribution(UUID pluginId) {
        return ratingRepository.countRatingDistributionByPluginId(pluginId);
    }

    @Override
    @Transactional
    public void markAsHelpful(UUID id) {
        ratingRepository.findByIdOptional(id).ifPresent(rating -> {
            rating.setHelpfulCount(rating.getHelpfulCount() + 1);
            ratingRepository.persist(rating);
        });
    }

    @Override
    @Transactional
    public void markAsVerified(UUID id) {
        ratingRepository.findByIdOptional(id).ifPresent(rating -> {
            rating.setIsVerified(true);
            ratingRepository.persist(rating);
        });
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ratingRepository.findByIdOptional(id).ifPresent(rating -> {
            UUID pluginId = rating.getPluginId();
            ratingRepository.delete(rating);
            
            // Update plugin rating statistics
            updatePluginRatingStatistics(pluginId);
        });
    }

    @Override
    public long countByPluginId(UUID pluginId) {
        return ratingRepository.countByPluginId(pluginId);
    }

    @Override
    public boolean hasRated(UUID pluginId, UUID userId) {
        return ratingRepository.existsByPluginIdAndUserId(pluginId, userId);
    }

    private void updatePluginRatingStatistics(UUID pluginId) {
        pluginRepository.findByIdOptional(pluginId).ifPresent(plugin -> {
            BigDecimal averageRating = ratingRepository.calculateAverageRatingByPluginId(pluginId);
            int ratingCount = (int) ratingRepository.countByPluginId(pluginId);

            plugin.setAverageRating(averageRating);
            plugin.setRatingCount(ratingCount);
            plugin.setUpdatedAt(LocalDateTime.now());

            pluginRepository.persist(plugin);
        });
    }
}