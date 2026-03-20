package com.easystation.alert.service;

import com.easystation.alert.domain.AlertChannel;
import com.easystation.alert.domain.AlertEvent;
import com.easystation.alert.domain.AlertRule;
import com.easystation.alert.dto.AlertEventRecord;
import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.easystation.alert.enums.AlertStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AlertEventService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AlertNotificationService notificationService;

    public List<AlertEventRecord.Detail> list(AlertEventRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Parameters params = new Parameters();

        if (query.eventType() != null) {
            sql.append(" and eventType = :eventType");
            params.and("eventType", query.eventType());
        }
        if (query.level() != null) {
            sql.append(" and level = :level");
            params.and("level", query.level());
        }
        if (query.status() != null) {
            sql.append(" and status = :status");
            params.and("status", query.status());
        }
        if (query.environmentId() != null) {
            sql.append(" and environmentId = :environmentId");
            params.and("environmentId", query.environmentId());
        }
        if (query.resourceId() != null) {
            sql.append(" and resourceId = :resourceId");
            params.and("resourceId", query.resourceId());
        }
        if (query.resourceType() != null && !query.resourceType().isBlank()) {
            sql.append(" and resourceType = :resourceType");
            params.and("resourceType", query.resourceType());
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and (title like :keyword or message like :keyword)");
            params.and("keyword", "%" + query.keyword() + "%");
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return AlertEvent.<AlertEvent>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AlertEventRecord.Detail get(UUID id) {
        AlertEvent event = AlertEvent.findById(id);
        if (event == null) {
            throw new WebApplicationException("Alert event not found", Response.Status.NOT_FOUND);
        }
        return toDetail(event);
    }

    @Transactional
    public AlertEventRecord.Detail create(AlertEventRecord.Create dto) {
        AlertEvent event = new AlertEvent();
        event.eventType = dto.eventType();
        event.level = dto.level();
        event.title = dto.title();
        event.message = dto.message();
        event.resourceId = dto.resourceId();
        event.resourceType = dto.resourceType();
        event.environmentId = dto.environmentId();
        event.ruleId = dto.ruleId();
        event.persist();

        // 触发通知
        notificationService.notify(event);

        return toDetail(event);
    }

    /**
     * 触发告警事件
     */
    @Transactional
    public AlertEventRecord.Detail trigger(AlertEventType eventType, AlertLevel level,
                                            String title, String message,
                                            UUID resourceId, String resourceType,
                                            UUID environmentId) {
        // 查找匹配的规则
        List<AlertRule> rules = AlertRule.<AlertRule>find(
                "eventType = ?1 and enabled = true", eventType)
                .list();

        AlertRule matchedRule = null;
        for (AlertRule rule : rules) {
            // TODO: 检查环境匹配和条件匹配
            matchedRule = rule;
            break;
        }

        AlertEvent event = new AlertEvent();
        event.eventType = eventType;
        event.level = level;
        event.title = title;
        event.message = message;
        event.resourceId = resourceId;
        event.resourceType = resourceType;
        event.environmentId = environmentId;
        if (matchedRule != null) {
            event.ruleId = matchedRule.id;
        }
        event.persist();

        // 触发通知
        notificationService.notify(event);

        return toDetail(event);
    }

    @Transactional
    public AlertEventRecord.Detail acknowledge(UUID id, AlertEventRecord.Acknowledge dto) {
        AlertEvent event = AlertEvent.findById(id);
        if (event == null) {
            throw new WebApplicationException("Alert event not found", Response.Status.NOT_FOUND);
        }
        if (event.status == AlertStatus.RESOLVED || event.status == AlertStatus.IGNORED) {
            throw new WebApplicationException("Cannot acknowledge a resolved or ignored event", Response.Status.BAD_REQUEST);
        }
        event.status = AlertStatus.ACKNOWLEDGED;
        event.acknowledgedBy = dto.acknowledgedBy();
        event.acknowledgedAt = LocalDateTime.now();
        return toDetail(event);
    }

    @Transactional
    public AlertEventRecord.Detail resolve(UUID id, AlertEventRecord.Resolve dto) {
        AlertEvent event = AlertEvent.findById(id);
        if (event == null) {
            throw new WebApplicationException("Alert event not found", Response.Status.NOT_FOUND);
        }
        if (event.status == AlertStatus.IGNORED) {
            throw new WebApplicationException("Cannot resolve an ignored event", Response.Status.BAD_REQUEST);
        }
        event.status = AlertStatus.RESOLVED;
        event.resolvedBy = dto.resolvedBy();
        event.resolvedAt = LocalDateTime.now();
        return toDetail(event);
    }

    @Transactional
    public AlertEventRecord.Detail ignore(UUID id) {
        AlertEvent event = AlertEvent.findById(id);
        if (event == null) {
            throw new WebApplicationException("Alert event not found", Response.Status.NOT_FOUND);
        }
        if (event.status == AlertStatus.RESOLVED) {
            throw new WebApplicationException("Cannot ignore a resolved event", Response.Status.BAD_REQUEST);
        }
        event.status = AlertStatus.IGNORED;
        return toDetail(event);
    }

    @Transactional
    public void delete(UUID id) {
        AlertEvent event = AlertEvent.findById(id);
        if (event == null) {
            throw new WebApplicationException("Alert event not found", Response.Status.NOT_FOUND);
        }
        event.delete();
    }

    public long countByStatus(AlertStatus status) {
        return AlertEvent.count("status", status);
    }

    private AlertEventRecord.Detail toDetail(AlertEvent event) {
        return new AlertEventRecord.Detail(
                event.id,
                event.eventType,
                event.level,
                event.status,
                event.title,
                event.message,
                event.resourceId,
                event.resourceType,
                event.environmentId,
                event.ruleId,
                event.count,
                event.lastNotifiedAt != null ? event.lastNotifiedAt.toString() : null,
                event.acknowledgedBy,
                event.acknowledgedAt != null ? event.acknowledgedAt.toString() : null,
                event.resolvedBy,
                event.resolvedAt != null ? event.resolvedAt.toString() : null,
                event.createdAt != null ? event.createdAt.toString() : null,
                event.updatedAt != null ? event.updatedAt.toString() : null
        );
    }
}