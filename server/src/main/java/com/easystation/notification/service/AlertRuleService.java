package com.easystation.notification.service;

import com.easystation.notification.domain.AlertRule;
import com.easystation.notification.dto.AlertRuleRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AlertRuleService {

    @Inject
    ObjectMapper objectMapper;

    public List<AlertRuleRecord> list() {
        return AlertRule.listAll().stream()
            .map(r -> (AlertRule) r)
            .map(this::toDto)
            .toList();
    }

    public AlertRuleRecord get(UUID id) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        return toDto(rule);
    }

    @Transactional
    public AlertRuleRecord create(AlertRuleRecord.Create dto) {
        if (AlertRule.count("name = ?1", dto.name()) > 0) {
            throw new WebApplicationException("Alert rule with same name already exists", Response.Status.CONFLICT);
        }
        AlertRule rule = new AlertRule();
        rule.setName(dto.name());
        rule.setMetric(dto.metric());
        rule.setConditionType(dto.conditionType());
        rule.setThreshold(dto.threshold());
        rule.setSeverity(dto.severity());
        rule.setNotificationChannelIds(toJson(dto.notificationChannelIds()));
        rule.setEnabled(dto.enabled() != null ? dto.enabled() : true);
        rule.persist();
        return toDto(rule);
    }

    @Transactional
    public AlertRuleRecord update(UUID id, AlertRuleRecord.Update dto) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null && !dto.name().equals(rule.name)) {
            if (AlertRule.count("name = ?1 and id != ?2", dto.name(), id) > 0) {
                throw new WebApplicationException("Alert rule with same name already exists", Response.Status.CONFLICT);
            }
            rule.setName(dto.name());
        }
        if (dto.metric() != null) rule.setMetric(dto.metric());
        if (dto.conditionType() != null) rule.setConditionType(dto.conditionType());
        if (dto.threshold() != null) rule.setThreshold(dto.threshold());
        if (dto.severity() != null) rule.setSeverity(dto.severity());
        if (dto.notificationChannelIds() != null) rule.setNotificationChannelIds(toJson(dto.notificationChannelIds()));
        if (dto.enabled() != null) rule.setEnabled(dto.enabled());
        return toDto(rule);
    }

    @Transactional
    public void delete(UUID id) {
        if (!AlertRule.deleteById(id)) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public AlertRuleRecord enable(UUID id) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        rule.setEnabled(true);
        return toDto(rule);
    }

    @Transactional
    public AlertRuleRecord disable(UUID id) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        rule.setEnabled(false);
        return toDto(rule);
    }

    private String toJson(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException("Failed to serialize notification channel IDs", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private List<UUID> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, UUID.class));
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private AlertRuleRecord toDto(AlertRule rule) {
        return new AlertRuleRecord(
            rule.id,
            rule.name,
            rule.metric,
            rule.conditionType,
            rule.threshold,
            rule.severity,
            parseJson(rule.notificationChannelIds),
            rule.enabled,
            rule.createdBy,
            rule.createdAt
        );
    }
}