package com.easystation.plugin.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plugin_rating")
@Getter
@Setter
public class PluginRating extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "plugin_id", nullable = false)
    public UUID pluginId;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, precision = 2, scale = 1)
    public BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    public String review;

    @Column(name = "is_verified")
    public Boolean isVerified = false;

    @Column(name = "helpful_count")
    public Integer helpfulCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Table(name = "plugin_rating", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plugin_id", "user_id"})
    })
    public static class Unique {}
}