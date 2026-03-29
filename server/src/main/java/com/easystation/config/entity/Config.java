package com.easystation.config.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 配置实体
 * 
 * 用于存储系统配置，支持热重载
 */
@Entity
@Table(name = "config")
public class Config extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "config_key", unique = true, nullable = false, length = 255)
    public String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    public String configValue;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "config_type", length = 50)
    public String configType;

    @Column(name = "version")
    public Integer version;

    @Column(name = "updated_by", length = 100)
    public String updatedBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * 根据 key 查找配置
     */
    public static Config findByKey(String configKey) {
        return find("configKey", configKey).firstResult();
    }

    /**
     * 检查 key 是否存在
     */
    public static boolean existsByKey(String configKey) {
        return count("configKey", configKey) > 0;
    }
}
