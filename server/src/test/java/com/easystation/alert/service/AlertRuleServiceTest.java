package com.easystation.alert.service;

import com.easystation.alert.dto.AlertRuleRecord;
import com.easystation.alert.enums.AlertEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertRuleServiceTest {

    private AlertRuleService alertRuleService;

    @BeforeEach
    void setUp() {
        alertRuleService = new AlertRuleService();
        alertRuleService.objectMapper = new ObjectMapper();
    }

    @Test
    void validateEmptyCondition() {
        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                null,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertEquals("Condition is valid (empty)", result.message());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateBlankCondition() {
        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                "   ",
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertEquals("Condition is valid (empty)", result.message());
    }

    @Test
    void validateInvalidJsonCondition() {
        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                "{ invalid json }",
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertFalse(result.valid());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).contains("Invalid JSON format"));
    }

    @Test
    void validateValidSimpleCondition() {
        String condition = """
                {
                    "field": "cpu_usage",
                    "operator": "gt",
                    "value": 80
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateConditionWithMissingField() {
        String condition = """
                {
                    "operator": "gt",
                    "value": 80
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertFalse(result.valid());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Missing 'field'")));
    }

    @Test
    void validateConditionWithMissingOperator() {
        String condition = """
                {
                    "field": "cpu_usage",
                    "value": 80
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertFalse(result.valid());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Missing 'operator'")));
    }

    @Test
    void validateConditionWithInvalidOperator() {
        String condition = """
                {
                    "field": "cpu_usage",
                    "operator": "invalid_op",
                    "value": 80
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertFalse(result.valid());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Invalid operator")));
    }

    @Test
    void validateAndCondition() {
        String condition = """
                {
                    "and": [
                        {
                            "field": "cpu_usage",
                            "operator": "gt",
                            "value": 80
                        },
                        {
                            "field": "memory_usage",
                            "operator": "gt",
                            "value": 70
                        }
                    ]
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateOrCondition() {
        String condition = """
                {
                    "or": [
                        {
                            "field": "status",
                            "operator": "eq",
                            "value": "down"
                        },
                        {
                            "field": "status",
                            "operator": "eq",
                            "value": "unreachable"
                        }
                    ]
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateNotCondition() {
        String condition = """
                {
                    "not": {
                        "field": "status",
                        "operator": "eq",
                        "value": "ok"
                    }
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateComplexNestedCondition() {
        String condition = """
                {
                    "and": [
                        {
                            "or": [
                                {
                                    "field": "cpu_usage",
                                    "operator": "gt",
                                    "value": 90
                                },
                                {
                                    "field": "memory_usage",
                                    "operator": "gt",
                                    "value": 90
                                }
                            ]
                        },
                        {
                            "not": {
                                "field": "maintenance_mode",
                                "operator": "eq",
                                "value": true
                            }
                        }
                    ]
                }
                """;

        AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                condition,
                AlertEventType.HOST_DOWN
        );

        AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validateAllOperators() {
        String[] operators = {"eq", "ne", "gt", "gte", "lt", "lte", "contains", 
                "not_contains", "starts_with", "ends_with", "in", "not_in", 
                "exists", "not_exists", "regex"};

        for (String op : operators) {
            String condition = String.format("""
                    {
                        "field": "test_field",
                        "operator": "%s",
                        "value": "test_value"
                    }
                    """, op);

            AlertRuleRecord.ValidateRequest request = new AlertRuleRecord.ValidateRequest(
                    condition,
                    AlertEventType.HOST_DOWN
            );

            AlertRuleRecord.ValidateResult result = alertRuleService.validate(request);
            assertTrue(result.valid(), "Operator '" + op + "' should be valid but got: " + result.errors());
        }
    }
}