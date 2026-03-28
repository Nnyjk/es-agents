package com.easystation.notification.domain;

import com.easystation.notification.enums.ChannelType;
import com.easystation.notification.enums.TemplateType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_template")
@Getter
@Setter
public class NotificationTemplate extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public TemplateType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    public ChannelType channelType;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String content;

    @Column(columnDefinition = "TEXT")
    public String variables;

    @Column(name = "created_by")
    public UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}