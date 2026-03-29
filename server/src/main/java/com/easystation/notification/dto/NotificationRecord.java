package com.easystation.notification.dto;

import com.easystation.notification.enums.MessageType;
import com.easystation.notification.enums.MessageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 站内消息 DTO
 */
public class NotificationRecord {

    /**
     * 创建消息请求
     */
    public record Create(
            @NotNull UUID userId,
            @NotBlank String username,
            @NotBlank String title,
            @NotBlank String content,
            @NotNull MessageType type,
            MessageLevel level,
            String relatedType,
            UUID relatedId,
            String jumpUrl
    ) {}

    /**
     * 查询参数
     */
    public record Query(
            UUID userId,
            MessageType type,
            MessageLevel level,
            Boolean isRead,
            String relatedType,
            UUID relatedId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String keyword,
            Integer limit,
            Integer offset
    ) {}

    /**
     * 消息详情
     */
    public record Detail(
            UUID id,
            UUID userId,
            String username,
            String title,
            String content,
            MessageType type,
            MessageLevel level,
            Boolean isRead,
            String relatedType,
            UUID relatedId,
            String jumpUrl,
            LocalDateTime readAt,
            LocalDateTime createdAt
    ) {}

    /**
     * 消息列表项
     */
    public record ListItem(
            UUID id,
            String title,
            MessageType type,
            MessageLevel level,
            Boolean isRead,
            String relatedType,
            LocalDateTime createdAt
    ) {}

    /**
     * 未读数量统计
     */
    public record UnreadCount(
            Long total,
            Long systemCount,
            Long alertCount,
            Long operationCount
    ) {}

    /**
     * 批量标记已读请求
     */
    public record MarkReadRequest(
            List<UUID> messageIds
    ) {}

    /**
     * 批量删除请求
     */
    public record BatchDeleteRequest(
            List<UUID> messageIds
    ) {}

    /**
     * 消息统计
     */
    public record Statistics(
            Long totalCount,
            Long readCount,
            Long unreadCount,
            Long todayCount
    ) {}
}
