package com.easystation.notification.service;

import com.easystation.notification.domain.NotificationHistory;
import com.easystation.notification.dto.HistoryRecord;
import com.easystation.notification.enums.NotificationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationHistoryService {

    public List<HistoryRecord> list(HistoryRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder();
        java.util.Map<String, Object> params = new java.util.HashMap<>();

        if (query.getChannelId() != null) {
            queryBuilder.append("channelId = :channelId");
            params.put("channelId", query.getChannelId());
        }
        if (query.getStatus() != null) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("status = :status");
            params.put("status", query.getStatus());
        }
        if (query.getStartTime() != null) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("createdAt >= :startTime");
            params.put("startTime", query.getStartTime());
        }
        if (query.getEndTime() != null) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("createdAt <= :endTime");
            params.put("endTime", query.getEndTime());
        }

        List<NotificationHistory> histories;
        if (queryBuilder.length() > 0) {
            histories = NotificationHistory.list(queryBuilder.toString(), params);
        } else {
            histories = NotificationHistory.listAll();
        }

        return histories.stream()
            .map(this::toDto)
            .toList();
    }

    public HistoryRecord get(UUID id) {
        NotificationHistory history = NotificationHistory.findById(id);
        if (history == null) {
            throw new WebApplicationException("Notification history not found", Response.Status.NOT_FOUND);
        }
        return toDto(history);
    }

    @Transactional
    public void delete(UUID id) {
        if (!NotificationHistory.deleteById(id)) {
            throw new WebApplicationException("Notification history not found", Response.Status.NOT_FOUND);
        }
    }

    private HistoryRecord toDto(NotificationHistory history) {
        return new HistoryRecord(
            history.id,
            history.channelId,
            history.templateId,
            history.recipient,
            history.title,
            history.content,
            history.status,
            history.sentAt,
            history.errorMessage,
            history.retryCount,
            history.createdAt
        );
    }
}