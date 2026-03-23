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
 * 部署策略配置实体 - 滚动发布、蓝绿发布、金丝雀发布等策略配置
 */
@Entity
@Table(name = "deploy_strategy", indexes = {
    @Index(name = "idx_deploy_strategy_app", columnList = "application_id"),
    @Index(name = "idx_deploy_strategy_env", columnList = "environment_id")
})
@Getter
@Setter
public class DeployStrategy extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "environment_id")
    public UUID environmentId;

    @Column(nullable = false)
    public String name;

    @Column(name = "strategy_type")
    @Enumerated(EnumType.STRING)
    public StrategyType type = StrategyType.ROLLING;

    @Column(name = "deploy_config", columnDefinition = "TEXT")
    public String deployConfig;  // JSON 格式存储部署配置

    @Column(name = "health_check_config", columnDefinition = "TEXT")
    public String healthCheckConfig;  // JSON 格式存储健康检查配置

    @Column(name = "rollback_config", columnDefinition = "TEXT")
    public String rollbackConfig;  // JSON 格式存储回滚配置

    @Column(name = "is_default")
    public Boolean isDefault = false;

    @Column(name = "active")
    public Boolean active = true;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public enum StrategyType {
        ROLLING,      // 滚动发布
        BLUE_GREEN,   // 蓝绿发布
        CANARY,       // 金丝雀发布
        RECREATE,     // 停机发布
        A_B_TESTING   // A/B 测试
    }
}