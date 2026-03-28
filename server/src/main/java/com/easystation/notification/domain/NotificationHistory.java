package com.easystation.notification.domain;

import com.easystation.notification.enums.NotificationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_history")
@Getter
@Setter
public class NotificationHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "channel_id", nullable = false)
    public UUID channelId;

    @Column(name = "template_id")
    public UUID templateId;

    @Column(nullable = false)
    public String recipient;

    @Column(nullable = false)
    public String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    public LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "retry_count", nullable = false)
    public Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}