package com.easystation.auth.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 权限实体
 * 
 * 支持细粒度权限控制：
 * - 资源级：plugin, host, deployment 等
 * - 操作级：read, create, update, delete
 * - 数据范围：ALL, DEPARTMENT, PROJECT, SELF
 */
@Entity
@Table(name = "permissions")
public class Permission extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "uuid")
    public UUID id;

    /**
     * 权限编码，格式：resource:action
     * 例如：plugin:read, plugin:create, host:delete
     */
    @Column(unique = true, nullable = false, length = 100)
    public String code;

    /**
     * 权限名称
     */
    @Column(nullable = false, length = 200)
    public String name;

    /**
     * 资源类型
     * 例如：plugin, host, deployment, user, role
     */
    @Column(nullable = false, length = 50)
    public String resource;

    /**
     * 操作类型
     * 例如：read, create, update, delete, manage
     */
    @Column(nullable = false, length = 20)
    public String action;

    /**
     * 数据范围
     * ALL - 全部数据
     * DEPARTMENT - 本部门数据
     * PROJECT - 本项目数据
     * SELF - 仅本人数据
     */
    @Column(name = "data_scope", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    public DataScope dataScope;

    /**
     * 权限描述
     */
    @Column(columnDefinition = "TEXT")
    public String description;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (code == null && resource != null && action != null) {
            code = resource + ":" + action;
        }
    }

    /**
     * 数据范围枚举
     */
    public enum DataScope {
        /** 全部数据 */
        ALL,
        /** 本部门数据 */
        DEPARTMENT,
        /** 本项目数据 */
        PROJECT,
        /** 仅本人数据 */
        SELF
    }

    /**
     * 检查是否有指定操作权限
     */
    public boolean hasAction(String action) {
        return this.action.equalsIgnoreCase(action) || 
               "manage".equalsIgnoreCase(this.action);
    }

    /**
     * 检查是否有指定资源权限
     */
    public boolean hasResource(String resource) {
        return this.resource.equalsIgnoreCase(resource);
    }
}
