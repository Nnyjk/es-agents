package com.easystation.plugin.repository;

import com.easystation.plugin.domain.PluginRating;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PluginRatingRepository implements PanacheRepositoryBase<PluginRating, UUID> {

    public List<PluginRating> findByPluginId(UUID pluginId) {
        return list("plugin.id", pluginId);
    }

    public List<PluginRating> findByPluginIdOrderByCreatedAtDesc(UUID pluginId) {
        return list("plugin.id = ?1 ORDER BY createdAt DESC", pluginId);
    }

    public List<PluginRating> findByPluginIdOrderByHelpfulCountDesc(UUID pluginId) {
        return list("plugin.id = ?1 ORDER BY helpfulCount DESC", pluginId);
    }

    public List<PluginRating> findByUserId(UUID userId) {
        return list("user.id", userId);
    }

    public Optional<PluginRating> findByPluginIdAndUserId(UUID pluginId, UUID userId) {
        return find("plugin.id = ?1 and user.id = ?2", pluginId, userId).firstResultOptional();
    }

    public List<PluginRating> findByPluginIdAndIsVerifiedTrue(UUID pluginId) {
        return list("plugin.id = ?1 and isVerified = true", pluginId);
    }

    public long countByPluginId(UUID pluginId) {
        return count("plugin.id", pluginId);
    }

    public long countByUserId(UUID userId) {
        return count("user.id", userId);
    }

    public long countByPluginIdAndIsVerifiedTrue(UUID pluginId) {
        return count("plugin.id = ?1 and isVerified = true", pluginId);
    }

    public boolean existsByPluginIdAndUserId(UUID pluginId, UUID userId) {
        return count("plugin.id = ?1 and user.id = ?2", pluginId, userId) > 0;
    }

    public BigDecimal calculateAverageRatingByPluginId(UUID pluginId) {
        Object result = getEntityManager()
            .createQuery("SELECT AVG(r.rating) FROM PluginRating r WHERE r.plugin.id = :pluginId")
            .setParameter("pluginId", pluginId)
            .getSingleResult();
        return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
    }

    public int[] countRatingDistributionByPluginId(UUID pluginId) {
        int[] distribution = new int[5];
        List<Object[]> results = getEntityManager()
            .createQuery("SELECT FLOOR(r.rating) as rating_floor, COUNT(r) FROM PluginRating r WHERE r.plugin.id = :pluginId GROUP BY FLOOR(r.rating)")
            .setParameter("pluginId", pluginId)
            .getResultList();
        for (Object[] row : results) {
            int floor = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            if (floor >= 1 && floor <= 5) {
                distribution[floor - 1] = count;
            }
        }
        return distribution;
    }
}