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
 * 应用配置实体 - 管理各环境的配置文件和环境变量
 */
@Entity
@Table(name = "application_config", indexes = {
    @Index(name = "idx_app_config_app", columnList = "application_id"),
    @Index(name = "idx_app_config_env", columnList = "environment_id"),
    @Index(name = "idx_app_config_key", columnList = "config_key")
})
@Getter
@Setter
public class ApplicationConfig extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "environment_id")
    public UUID environmentId;

    @Column(name = "config_type")
    @Enumerated(EnumType.STRING)
    public ConfigType configType = ConfigType.ENV_VAR;

    @Column(name = "config_key", nullable = false)
    public String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    public String configValue;

    @Column(name = "value_type")
    @Enumerated(EnumType.STRING)
    public ValueType valueType = ValueType.STRING;

    @Column(name = "is_secret")
    public Boolean isSecret = false;

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

    public enum ConfigType {
        ENV_VAR,      // 环境变量
        CONFIG_FILE,  // 配置文件
        SECRET,       // 敏感配置
        MOUNT         // 挂载配置
    }

    public enum ValueType {
        STRING,       // 字符串
        NUMBER,       // 数字
        BOOLEAN,      // 布尔
        JSON,         // JSON对象
        YAML,         // YAML
        BASE64        // Base64编码
    }
}