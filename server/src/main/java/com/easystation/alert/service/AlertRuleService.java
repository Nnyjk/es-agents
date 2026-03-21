package com.easystation.alert.service;

import com.easystation.alert.domain.AlertRule;
import com.easystation.alert.dto.AlertRuleRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AlertRuleService {

    @Inject
    ObjectMapper objectMapper;

    public List<AlertRuleRecord.Detail> list() {
        return AlertRule.<AlertRule>listAll().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public List<AlertRuleRecord.Detail> listEnabled() {
        return AlertRule.<AlertRule>find("enabled", true).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AlertRuleRecord.Detail get(UUID id) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        return toDetail(rule);
    }

    @Transactional
    public AlertRuleRecord.Detail create(AlertRuleRecord.Create dto) {
        AlertRule rule = new AlertRule();
        rule.name = dto.name();
        rule.description = dto.description();
        rule.eventType = dto.eventType();
        rule.level = dto.level();
        rule.condition = dto.condition();
        rule.environmentIds = toJsonUuid(dto.environmentIds());
        if (dto.channelIds() != null) {
            rule.channelIds = dto.channelIds();
        }
        if (dto.enabled() != null) {
            rule.enabled = dto.enabled();
        }
        rule.persist();
        return toDetail(rule);
    }

    @Transactional
    public AlertRuleRecord.Detail update(UUID id, AlertRuleRecord.Update dto) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        if (dto.name() != null) rule.name = dto.name();
        if (dto.description() != null) rule.description = dto.description();
        if (dto.eventType() != null) rule.eventType = dto.eventType();
        if (dto.level() != null) rule.level = dto.level();
        if (dto.condition() != null) rule.condition = dto.condition();
        if (dto.environmentIds() != null) rule.environmentIds = toJsonUuid(dto.environmentIds());
        if (dto.channelIds() != null) rule.channelIds = dto.channelIds();
        if (dto.enabled() != null) rule.enabled = dto.enabled();
        return toDetail(rule);
    }

    @Transactional
    public void delete(UUID id) {
        AlertRule rule = AlertRule.findById(id);
        if (rule == null) {
            throw new WebApplicationException("Alert rule not found", Response.Status.NOT_FOUND);
        }
        rule.delete();
    }

    private AlertRuleRecord.Detail toDetail(AlertRule rule) {
        return new AlertRuleRecord.Detail(
                rule.id,
                rule.name,
                rule.description,
                rule.eventType,
                rule.level,
                rule.condition,
                fromJsonUuid(rule.environmentIds),
                rule.channelIds,
                rule.enabled,
                rule.createdAt != null ? rule.createdAt.toString() : null,
                rule.updatedAt != null ? rule.updatedAt.toString() : null
        );
    }

    private String toJsonUuid(List<UUID> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list.stream().map(UUID::toString).toList());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<UUID> fromJsonUuid(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<String> strings = objectMapper.readValue(json, new TypeReference<>() {});
            return strings.stream().map(UUID::fromString).toList();
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}