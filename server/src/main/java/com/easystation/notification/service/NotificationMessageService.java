package com.easystation.notification.service;

import com.easystation.notification.domain.NotificationMessage;
import com.easystation.notification.dto.NotificationRecord;
import com.easystation.notification.enums.MessageLevel;
import com.easystation.notification.enums.MessageType;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 站内消息服务
 */
@ApplicationScoped
public class NotificationMessageService {

    /**
     * 创建消息
     */
    @Transactional
    public NotificationMessage create(NotificationRecord.Create dto) {
        NotificationMessage message = new NotificationMessage();
        message.userId = dto.userId();
        message.username = dto.username();
        message.title = dto.title();
        message.content = dto.content();
        message.type = dto.type();
        message.level = dto.level();
        message.relatedType = dto.relatedType();
        message.relatedId = dto.relatedId();
        message.jumpUrl = dto.jumpUrl();
        message.isRead = false;
        message.deleted = false;
        message.persist();
        return message;
    }

    /**
     * 根据 ID 查询详情
     */
    public NotificationRecord.Detail findById(UUID id) {
        NotificationMessage message = NotificationMessage.findById(id);
        if (message == null) {
            return null;
        }
        return toDetail(message);
    }

    /**
     * 分页查询消息列表
     */
    public List<NotificationRecord.ListItem> findAll(NotificationRecord.Query query) {
        StringBuilder sql = new StringBuilder("deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (query.userId() != null) {
            sql.append(" and userId = :userId");
            params.put("userId", query.userId());
        }

        if (query.type() != null) {
            sql.append(" and type = :type");
            params.put("type", query.type());
        }

        if (query.level() != null) {
            sql.append(" and level = :level");
            params.put("level", query.level());
        }

        if (query.isRead() != null) {
            sql.append(" and isRead = :isRead");
            params.put("isRead", query.isRead());
        }

        if (query.relatedType() != null) {
            sql.append(" and relatedType = :relatedType");
            params.put("relatedType", query.relatedType());
        }

        if (query.relatedId() != null) {
            sql.append(" and relatedId = :relatedId");
            params.put("relatedId", query.relatedId());
        }

        if (query.startTime() != null) {
            sql.append(" and createdAt >= :startTime");
            params.put("startTime", query.startTime());
        }

        if (query.endTime() != null) {
            sql.append(" and createdAt <= :endTime");
            params.put("endTime", query.endTime());
        }

        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and (title like :keyword or content like :keyword)");
            params.put("keyword", "%" + query.keyword() + "%");
        }

        PanacheQuery<NotificationMessage> panacheQuery = NotificationMessage.find(
                sql.toString(),
                Sort.by("createdAt", Sort.Direction.Descending),
                params
        );

        // 分页
        if (query.limit() != null && query.limit() > 0) {
            int page = query.offset() != null ? query.offset() / query.limit() : 0;
            panacheQuery.page(Page.of(page, query.limit()));
        }

        List<NotificationMessage> messages = panacheQuery.list();
        return messages.stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public boolean markAsRead(UUID id) {
        NotificationMessage message = NotificationMessage.findById(id);
        if (message == null || message.isRead) {
            return false;
        }
        message.isRead = true;
        message.readAt = LocalDateTime.now();
        message.persist();
        return true;
    }

    /**
     * 批量标记消息为已读
     */
    @Transactional
    public int markBatchAsRead(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        String hql = "UPDATE NotificationMessage m SET m.isRead = true, m.readAt = :readAt " +
                     "WHERE m.id IN :ids AND m.isRead = false";
        
        long updated = NotificationMessage.update(
                hql,
                Map.of("readAt", LocalDateTime.now(), "ids", ids)
        );
        
        return (int) updated;
    }

    /**
     * 删除消息（软删除）
     */
    @Transactional
    public boolean delete(UUID id) {
        NotificationMessage message = NotificationMessage.findById(id);
        if (message == null || message.deleted) {
            return false;
        }
        message.deleted = true;
        message.persist();
        return true;
    }

    /**
     * 批量删除消息（软删除）
     */
    @Transactional
    public int deleteBatch(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        String hql = "UPDATE NotificationMessage m SET m.deleted = true " +
                     "WHERE m.id IN :ids AND m.deleted = false";
        
        long updated = NotificationMessage.update(hql, Map.of("ids", ids));
        return (int) updated;
    }

    /**
     * 获取用户未读消息数量
     */
    public NotificationRecord.UnreadCount getUnreadCount(UUID userId) {
        long total = NotificationMessage.count("userId = ?1 and isRead = false and deleted = false", userId);
        long systemCount = NotificationMessage.count(
                "userId = ?1 and type = ?2 and isRead = false and deleted = false",
                userId, MessageType.SYSTEM
        );
        long alertCount = NotificationMessage.count(
                "userId = ?1 and type = ?2 and isRead = false and deleted = false",
                userId, MessageType.ALERT
        );
        long operationCount = NotificationMessage.count(
                "userId = ?1 and type = ?2 and isRead = false and deleted = false",
                userId, MessageType.OPERATION
        );

        return new NotificationRecord.UnreadCount(total, systemCount, alertCount, operationCount);
    }

    /**
     * 获取用户消息统计
     */
    public NotificationRecord.Statistics getStatistics(UUID userId) {
        long totalCount = NotificationMessage.count("userId = ?1 and deleted = false", userId);
        long readCount = NotificationMessage.count(
                "userId = ?1 and isRead = true and deleted = false", userId
        );
        long unreadCount = totalCount - readCount;
        
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = NotificationMessage.count(
                "userId = ?1 and createdAt >= ?2 and deleted = false",
                userId, todayStart
        );

        return new NotificationRecord.Statistics(totalCount, readCount, unreadCount, todayCount);
    }

    /**
     * 转换为 Detail DTO
     */
    private NotificationRecord.Detail toDetail(NotificationMessage message) {
        return new NotificationRecord.Detail(
                message.id,
                message.userId,
                message.username,
                message.title,
                message.content,
                message.type,
                message.level,
                message.isRead,
                message.relatedType,
                message.relatedId,
                message.jumpUrl,
                message.readAt,
                message.createdAt
        );
    }

    /**
     * 转换为 ListItem DTO
     */
    private NotificationRecord.ListItem toListItem(NotificationMessage message) {
        return new NotificationRecord.ListItem(
                message.id,
                message.title,
                message.type,
                message.level,
                message.isRead,
                message.relatedType,
                message.createdAt
        );
    }
}
