package com.easystation.notification.domain;

import com.easystation.notification.enums.ConditionType;
import com.easystation.notification.enums.Severity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "NotificationAlertRule")
@Table(name = "alert_rule")
@Getter
@Setter
public class AlertRule extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String name;

    @Column(nullable = false)
    public String metric;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    public ConditionType conditionType;

    @Column(nullable = false)
    public String threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Severity severity;

    @Column(name = "notification_channel_ids", columnDefinition = "TEXT")
    public String notificationChannelIds;

    @Column(nullable = false)
    public Boolean enabled = true;

    @Column(name = "created_by")
    public UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}