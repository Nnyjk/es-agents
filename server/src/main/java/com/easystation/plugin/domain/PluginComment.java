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
@Table(name = "plugin_comment")
@Getter
@Setter
public class PluginComment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "plugin_id", nullable = false)
    public UUID pluginId;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "parent_id")
    public UUID parentId;

    @Column(name = "reply_to_user_id")
    public UUID replyToUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @Column(name = "is_developer_reply")
    public Boolean isDeveloperReply = false;

    @Column(name = "is_pinned")
    public Boolean isPinned = false;

    @Column(name = "is_hidden")
    public Boolean isHidden = false;

    @Column(name = "like_count")
    public Integer likeCount = 0;

    @Column(name = "reply_count")
    public Integer replyCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}