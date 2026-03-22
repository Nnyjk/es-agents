package com.easystation.alert.service;

import com.easystation.alert.domain.AlertSilence;
import com.easystation.alert.dto.AlertSilenceRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AlertSilenceService {

    @Inject
    ObjectMapper objectMapper;

    public List<AlertSilenceRecord.Detail> list() {
        return AlertSilence.<AlertSilence>listAll().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public List<AlertSilenceRecord.Detail> listEnabled() {
        return AlertSilence.<AlertSilence>find("enabled", true).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public List<AlertSilenceRecord.Detail> listActive(LocalDateTime at) {
        if (at == null) {
            at = LocalDateTime.now();
        }
        return AlertSilence.<AlertSilence>find(
                "enabled = true and silenceStart <= ?1 and silenceEnd >= ?1",
                at
        ).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public AlertSilenceRecord.Detail get(UUID id) {
        AlertSilence silence = AlertSilence.findById(id);
        if (silence == null) {
            throw new WebApplicationException("Alert silence not found", Response.Status.NOT_FOUND);
        }
        return toDetail(silence);
    }

    @Transactional
    public AlertSilenceRecord.Detail create(AlertSilenceRecord.Create dto) {
        AlertSilence silence = new AlertSilence();
        silence.name = dto.name();
        silence.description = dto.description();
        silence.matchCondition = dto.matchCondition();
        silence.silenceStart = dto.silenceStart();
        silence.silenceEnd = dto.silenceEnd();
        silence.durationSeconds = dto.durationSeconds();
        if (dto.enabled() != null) {
            silence.enabled = dto.enabled();
        }
        silence.persist();
        return toDetail(silence);
    }

    @Transactional
    public AlertSilenceRecord.Detail update(UUID id, AlertSilenceRecord.Update dto) {
        AlertSilence silence = AlertSilence.findById(id);
        if (silence == null) {
            throw new WebApplicationException("Alert silence not found", Response.Status.NOT_FOUND);
        }

        if (dto.name() != null) silence.name = dto.name();
        if (dto.description() != null) silence.description = dto.description();
        if (dto.matchCondition() != null) silence.matchCondition = dto.matchCondition();
        if (dto.silenceStart() != null) silence.silenceStart = dto.silenceStart();
        if (dto.silenceEnd() != null) silence.silenceEnd = dto.silenceEnd();
        if (dto.durationSeconds() != null) silence.durationSeconds = dto.durationSeconds();
        if (dto.enabled() != null) silence.enabled = dto.enabled();

        silence.persist();
        return toDetail(silence);
    }

    @Transactional
    public void delete(UUID id) {
        AlertSilence silence = AlertSilence.findById(id);
        if (silence == null) {
            throw new WebApplicationException("Alert silence not found", Response.Status.NOT_FOUND);
        }
        silence.delete();
    }

    /**
     * 检查告警是否应该被静默
     * @param eventType 告警事件类型
     * @param level 告警级别
     * @param source 告警来源
     * @param tags 告警标签
     * @return 如果应该静默返回 true
     */
    public boolean shouldSilence(String eventType, String level, String source, List<String> tags) {
        List<AlertSilence> activeSilences = AlertSilence.find(
                "enabled = true and silenceStart <= ?1 and silenceEnd >= ?1",
                LocalDateTime.now()
        ).list();

        for (AlertSilence silence : activeSilences) {
            if (matchCondition(silence.matchCondition, eventType, level, source, tags)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配静默条件
     */
    private boolean matchCondition(String matchConditionJson, String eventType, String level,
                                   String source, List<String> tags) {
        if (matchConditionJson == null || matchConditionJson.isBlank()) {
            return true; // 无条件表示匹配所有
        }

        try {
            AlertSilenceRecord.MatchCondition condition = objectMapper.readValue(
                    matchConditionJson,
                    AlertSilenceRecord.MatchCondition.class
            );

            // 检查事件类型
            if (condition.eventTypes() != null && !condition.eventTypes().isEmpty()) {
                boolean matched = condition.eventTypes().stream()
                        .anyMatch(et -> et.name().equals(eventType));
                if (!matched) return false;
            }

            // 检查告警级别
            if (condition.levels() != null && !condition.levels().isEmpty()) {
                boolean matched = condition.levels().stream()
                        .anyMatch(l -> l.name().equals(level));
                if (!matched) return false;
            }

            // 检查来源
            if (condition.sources() != null && !condition.sources().isEmpty()) {
                if (source == null || !condition.sources().contains(source)) {
                    return false;
                }
            }

            // 检查标签
            if (condition.tags() != null && !condition.tags().isEmpty()) {
                if (tags == null || tags.isEmpty()) {
                    return false;
                }
                boolean tagMatched = condition.tags().stream()
                        .anyMatch(tags::contains);
                if (!tagMatched) return false;
            }

            return true;
        } catch (JsonProcessingException e) {
            // 条件解析失败，返回 false
            return false;
        }
    }

    private AlertSilenceRecord.Detail toDetail(AlertSilence silence) {
        return new AlertSilenceRecord.Detail(
                silence.id,
                silence.name,
                silence.description,
                silence.matchCondition,
                silence.silenceStart,
                silence.silenceEnd,
                silence.durationSeconds,
                silence.enabled,
                silence.createdBy,
                silence.createdAt,
                silence.updatedAt
        );
    }
}