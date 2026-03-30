package com.easystation.config.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "ConfigDomainHistory")
@Table(name = "config_history")
@Getter
@Setter
public class ConfigHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "config_id", nullable = false)
    public UUID configId;

    @Column(name = "config_key")
    public String configKey;

    @Column(columnDefinition = "TEXT")
    public String oldValue;

    @Column(columnDefinition = "TEXT")
    public String newValue;

    @Column(name = "change_type", nullable = false)
    public String changeType;

    public Integer version;

    @Column(name = "changed_by")
    public String changedBy;

    @Column(name = "change_reason")
    public String changeReason;

    @Column(name = "environment_id")
    public UUID environmentId;

    @CreationTimestamp
    @Column(name = "changed_at")
    public LocalDateTime changedAt;
}