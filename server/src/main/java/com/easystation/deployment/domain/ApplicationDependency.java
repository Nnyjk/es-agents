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
 * 应用依赖实体 - 管理应用依赖的中间件、数据库、服务等
 */
@Entity
@Table(name = "application_dependency", indexes = {
    @Index(name = "idx_app_dep_app", columnList = "application_id"),
    @Index(name = "idx_app_dep_env", columnList = "environment_id")
})
@Getter
@Setter
public class ApplicationDependency extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "environment_id")
    public UUID environmentId;

    @Column(name = "dep_type")
    @Enumerated(EnumType.STRING)
    public DependencyType type = DependencyType.SERVICE;

    @Column(name = "dep_name", nullable = false)
    public String dependencyName;

    @Column(name = "dep_version")
    public String dependencyVersion;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "active")
    public Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public enum DependencyType {
        DATABASE,      // 数据库
        CACHE,         // 缓存
        MESSAGE_QUEUE, // 消息队列
        STORAGE,       // 存储服务
        SERVICE,       // 内部服务
        EXTERNAL_API,  // 外部 API
        OTHER          // 其他
    }
}