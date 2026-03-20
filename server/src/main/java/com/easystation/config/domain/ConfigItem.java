package com.easystation.config.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "config_item")
@Getter
@Setter
public class ConfigItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String key;

    @Column(columnDefinition = "TEXT")
    public String value;

    @Column(nullable = false)
    public String type;

    public String description;

    @Column(name = "environment_id")
    public UUID environmentId;

    public String group;

    @Column(name = "is_encrypted")
    public boolean encrypted;

    @Column(name = "is_active")
    public boolean active;

    @Version
    public Integer version;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}