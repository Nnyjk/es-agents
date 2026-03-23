package com.easystation.deployment.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 制品仓库配置实体 - Docker/NPM/Maven 等私有仓库配置
 */
@Entity
@Table(name = "artifact_repository", indexes = {
    @Index(name = "idx_artifact_repo_type", columnList = "type"),
    @Index(name = "idx_artifact_repo_name", columnList = "name")
})
@Getter
@Setter
public class ArtifactRepository extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false, unique = true)
    public String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public RepositoryType type;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    public String url;

    @Column(name = "credential_id")
    public UUID credentialId;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "active")
    public Boolean active = true;

    @Column(name = "is_default")
    public Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public enum RepositoryType {
        DOCKER,   // Docker 镜像仓库
        NPM,      // NPM 私有仓库
        MAVEN,    // Maven 私有仓库
        PYPI,     // PyPI 私有仓库
        HELM,     // Helm Chart 仓库
        GENERIC   // 通用制品仓库
    }
}