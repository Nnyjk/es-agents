package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginRatingRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginRatingService {

    PluginRatingRecord create(PluginRatingRecord.Create create, UUID userId);

    PluginRatingRecord update(UUID id, PluginRatingRecord.Update update);

    Optional<PluginRatingRecord> findById(UUID id);

    Optional<PluginRatingRecord> findByPluginIdAndUserId(UUID pluginId, UUID userId);

    List<PluginRatingRecord> findByPluginId(UUID pluginId);

    List<PluginRatingRecord> findByUserId(UUID userId);

    List<PluginRatingRecord> search(PluginRatingRecord.Query query);

    PluginRatingRecord.Summary getSummary(UUID pluginId);

    BigDecimal getAverageRating(UUID pluginId);

    int[] getRatingDistribution(UUID pluginId);

    void markAsHelpful(UUID id);

    void markAsVerified(UUID id);

    void delete(UUID id);

    long countByPluginId(UUID pluginId);

    boolean hasRated(UUID pluginId, UUID userId);
}