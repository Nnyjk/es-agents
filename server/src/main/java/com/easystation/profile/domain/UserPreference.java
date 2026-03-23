package com.easystation.profile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "user_preference")
@Getter
@Setter
public class UserPreference extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    public UUID userId;

    @Column(length = 50)
    public String theme = "light";

    @Column(length = 10)
    public String language = "zh-CN";

    @Column(length = 50)
    public String layout = "default";

    @Column(name = "default_page")
    public String defaultPage;

    @Column(name = "page_size")
    public Integer pageSize = 20;

    @Column(name = "default_sort")
    public String defaultSort;

    @Column(name = "display_fields", columnDefinition = "TEXT")
    public String displayFields;

    @Column(name = "quick_actions", columnDefinition = "TEXT")
    public String quickActions;

    @Column(name = "notification_enabled")
    public Boolean notificationEnabled = true;

    @Column(name = "email_notification")
    public Boolean emailNotification = true;

    @Column(name = "sms_notification")
    public Boolean smsNotification = false;

    @Column(name = "webhook_notification")
    public Boolean webhookNotification = false;

    @Column(name = "silent_hours_start")
    public LocalTime silentHoursStart;

    @Column(name = "silent_hours_end")
    public LocalTime silentHoursEnd;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}