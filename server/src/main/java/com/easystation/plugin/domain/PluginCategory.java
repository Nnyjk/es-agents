package com.easystation.plugin.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plugin_category")
@Getter
@Setter
public class PluginCategory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "parent_id")
    public UUID parentId;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(length = 50)
    public String code;

    @Column(length = 500)
    public String icon;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "sort_order")
    public Integer sortOrder = 0;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}