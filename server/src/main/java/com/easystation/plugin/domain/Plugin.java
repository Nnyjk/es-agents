package com.easystation.plugin.domain;

import com.easystation.plugin.domain.enums.PluginStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plugin")
@Getter
@Setter
public class Plugin extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "developer_id", nullable = false)
    public UUID developerId;

    @Column(name = "category_id")
    public UUID categoryId;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(length = 50, unique = true)
    public String code;

    @Column(length = 500)
    public String icon;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "readme", columnDefinition = "TEXT")
    public String readme;

    @Column(name = "source_url", length = 500)
    public String sourceUrl;

    @Column(name = "doc_url", length = 500)
    public String docUrl;

    @Column(name = "homepage_url", length = 500)
    public String homepageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public PluginStatus status = PluginStatus.DRAFT;

    @Column(name = "is_free")
    public Boolean isFree = true;

    @Column(name = "price")
    public java.math.BigDecimal price;

    @Column(name = "min_platform_version", length = 20)
    public String minPlatformVersion;

    @Column(name = "max_platform_version", length = 20)
    public String maxPlatformVersion;

    @Column(name = "supported_platforms", length = 500)
    public String supportedPlatforms;

    @Column(name = "permissions_required", columnDefinition = "TEXT")
    public String permissionsRequired;

    @Column(name = "config_schema", columnDefinition = "TEXT")
    public String configSchema;

    @Column(name = "total_downloads")
    public Long totalDownloads = 0L;

    @Column(name = "total_installs")
    public Long totalInstalls = 0L;

    @Column(name = "average_rating", precision = 3, scale = 2)
    public java.math.BigDecimal averageRating = java.math.BigDecimal.ZERO;

    @Column(name = "rating_count")
    public Integer ratingCount = 0;

    @Column(name = "comment_count")
    public Integer commentCount = 0;

    @Column(name = "favorite_count")
    public Integer favoriteCount = 0;

    @Column(name = "published_at")
    public LocalDateTime publishedAt;

    @Column(name = "deprecated_at")
    public LocalDateTime deprecatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}