package com.easystation.config.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配置历史实体
 * 
 * 记录配置变更历史，用于审计和回滚
 */
@Entity(name = "ConfigEntityHistory")
@Table(name = "config_history")
public class ConfigHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "config_id", nullable = false)
    public Long configId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    public String oldValue;

    @Column(name = "new_value", nullable = false, columnDefinition = "TEXT")
    public String newValue;

    @Column(name = "changed_by", length = 100)
    public String changedBy;

    @Column(name = "changed_at")
    public LocalDateTime changedAt;

    @Column(name = "change_reason", length = 500)
    public String changeReason;

    /**
     * 根据配置 ID 查找历史记录
     */
    public static List<ConfigHistory> findByConfigId(Long configId) {
        return find("configId ORDER BY changedAt DESC", configId).list();
    }

    /**
     * 查找最近的 N 条历史记录
     */
    public static List<ConfigHistory> findRecentByConfigId(Long configId, int limit) {
        return find("configId ORDER BY changedAt DESC", configId)
            .page(0, limit)
            .list();
    }
}
