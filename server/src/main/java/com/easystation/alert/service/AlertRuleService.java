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

import java.util.ArrayList;
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

    /**
     * 校验告警规则条件
     */
    public AlertRuleRecord.ValidateResult validate(AlertRuleRecord.ValidateRequest request) {
        List<String> errors = new ArrayList<>();

        // 如果没有条件，返回有效
        if (request.condition() == null || request.condition().isBlank()) {
            return new AlertRuleRecord.ValidateResult(true, "Condition is valid (empty)", errors);
        }

        // 尝试解析 JSON
        try {
            Object parsed = objectMapper.readValue(request.condition(), Object.class);

            // 检查是否为有效的条件结构
            if (parsed instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> conditionMap = (java.util.Map<String, Object>) parsed;

                // 验证条件字段
                validateConditionStructure(conditionMap, "", errors, request.eventType());
            } else if (parsed instanceof java.util.List) {
                // 条件可以是数组形式（多个条件的 AND/OR 组合）
                for (Object item : (java.util.List<?>) parsed) {
                    if (item instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) item;
                        validateConditionStructure(itemMap, "", errors, request.eventType());
                    }
                }
            }
        } catch (JsonProcessingException e) {
            errors.add("Invalid JSON format: " + e.getOriginalMessage());
        }

        boolean valid = errors.isEmpty();
        String message = valid
                ? "Condition is valid"
                : "Condition validation failed with " + errors.size() + " error(s)";

        return new AlertRuleRecord.ValidateResult(valid, message, errors);
    }

    /**
     * 验证条件结构
     */
    @SuppressWarnings("unchecked")
    private void validateConditionStructure(java.util.Map<String, Object> condition, String path,
                                            List<String> errors, com.easystation.alert.enums.AlertEventType eventType) {
        // 检查支持的操作符
        String[] supportedOperators = {"and", "or", "not", "field", "operator", "value"};

        for (String key : condition.keySet()) {
            String currentPath = path.isEmpty() ? key : path + "." + key;

            // 检查逻辑操作符
            if ("and".equals(key) || "or".equals(key)) {
                Object value = condition.get(key);
                if (!(value instanceof java.util.List)) {
                    errors.add("Operator '" + key + "' at path '" + currentPath + "' must be an array");
                } else {
                    for (Object item : (java.util.List<?>) value) {
                        if (item instanceof java.util.Map) {
                            validateConditionStructure((java.util.Map<String, Object>) item, currentPath, errors, eventType);
                        }
                    }
                }
            } else if ("not".equals(key)) {
                Object value = condition.get(key);
                if (!(value instanceof java.util.Map)) {
                    errors.add("Operator 'not' at path '" + currentPath + "' must be an object");
                } else {
                    validateConditionStructure((java.util.Map<String, Object>) value, currentPath, errors, eventType);
                }
            } else if ("field".equals(key)) {
                // 验证字段名是否有效（基于事件类型）
                String fieldName = String.valueOf(condition.get(key));
                if (fieldName == null || fieldName.isBlank()) {
                    errors.add("Field name at path '" + currentPath + "' cannot be empty");
                }
                // 可以根据 eventType 添加特定字段验证
            } else if ("operator".equals(key)) {
                // 验证操作符是否支持
                String operator = String.valueOf(condition.get(key));
                String[] validOperators = {"eq", "ne", "gt", "gte", "lt", "lte", "contains", "not_contains",
                        "starts_with", "ends_with", "in", "not_in", "exists", "not_exists", "regex"};
                boolean validOp = false;
                for (String op : validOperators) {
                    if (op.equals(operator)) {
                        validOp = true;
                        break;
                    }
                }
                if (!validOp) {
                    errors.add("Invalid operator '" + operator + "' at path '" + currentPath + "'");
                }
            } else if ("value".equals(key)) {
                // value 可以是任意类型，无需特殊验证
            } else {
                // 未知字段，可能是嵌套条件
                Object value = condition.get(key);
                if (value instanceof java.util.Map) {
                    validateConditionStructure((java.util.Map<String, Object>) value, currentPath, errors, eventType);
                }
            }
        }

        // 检查必需字段（对于叶子条件）
        if (condition.containsKey("field") || condition.containsKey("operator") || condition.containsKey("value")) {
            if (!condition.containsKey("field")) {
                errors.add("Missing 'field' in condition at path '" + path + "'");
            }
            if (!condition.containsKey("operator")) {
                errors.add("Missing 'operator' in condition at path '" + path + "'");
            }
        }
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