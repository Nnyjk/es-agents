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
@Table(name = "plugin_version")
@Getter
@Setter
public class PluginVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "plugin_id", nullable = false)
    public UUID pluginId;

    @Column(nullable = false, length = 20)
    public String version;

    @Column(name = "version_code")
    public Integer versionCode;

    @Column(columnDefinition = "TEXT")
    public String changelog;

    @Column(name = "download_url", length = 500)
    public String downloadUrl;

    @Column(name = "package_size")
    public Long packageSize;

    @Column(name = "package_hash", length = 128)
    public String packageHash;

    @Column(name = "signature_hash", length = 128)
    public String signatureHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public PluginStatus status = PluginStatus.DRAFT;

    @Column(name = "min_platform_version", length = 20)
    public String minPlatformVersion;

    @Column(name = "max_platform_version", length = 20)
    public String maxPlatformVersion;

    @Column(name = "dependencies", columnDefinition = "TEXT")
    public String dependencies;

    @Column(name = "resource_requirements", columnDefinition = "TEXT")
    public String resourceRequirements;

    @Column(name = "download_count")
    public Long downloadCount = 0L;

    @Column(name = "install_count")
    public Long installCount = 0L;

    @Column(name = "is_latest")
    public Boolean isLatest = false;

    @Column(name = "is_prerelease")
    public Boolean isPrerelease = false;

    @Column(name = "published_at")
    public LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}