package com.easystation.notification.domain;

import com.easystation.notification.enums.MessageType;
import com.easystation.notification.enums.MessageLevel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 站内消息
 */
@Entity
@Table(name = "notification_messages")
@Getter
@Setter
public class NotificationMessage extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 接收用户 ID
     */
    @Column(nullable = false)
    public UUID userId;

    /**
     * 接收用户名
     */
    @Column(nullable = false)
    public String username;

    /**
     * 消息标题
     */
    @Column(nullable = false)
    public String title;

    /**
     * 消息内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MessageType type;

    /**
     * 消息级别
     */
    @Enumerated(EnumType.STRING)
    public MessageLevel level;

    /**
     * 是否已读
     */
    @Column(nullable = false)
    public Boolean isRead = false;

    /**
     * 关联资源类型
     */
    public String relatedType;

    /**
     * 关联资源 ID
     */
    public UUID relatedId;

    /**
     * 跳转链接
     */
    public String jumpUrl;

    /**
     * 阅读时间
     */
    public LocalDateTime readAt;

    /**
     * 软删除标记
     */
    @Column(nullable = false)
    public Boolean deleted = false;

    /**
     * 创建时间（不可修改）
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;
}
