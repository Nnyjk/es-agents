package com.easystation.alert.service;

import com.easystation.alert.dto.AlertSilenceRecord;
import com.easystation.alert.enums.AlertEventType;
import com.easystation.alert.enums.AlertLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertSilenceServiceTest {

    private AlertSilenceService alertSilenceService;

    @BeforeEach
    void setUp() {
        alertSilenceService = new AlertSilenceService();
        alertSilenceService.objectMapper = new ObjectMapper();
    }

    @Test
    void matchEmptyCondition() {
        // 无条件（null）时应该匹配所有
        assertTrue(alertSilenceService.matchCondition(
                null,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "host-1",
                Collections.emptyList()
        ));

        // 空字符串条件也应该匹配所有
        assertTrue(alertSilenceService.matchCondition(
                "",
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "host-1",
                Collections.emptyList()
        ));

        // 空白字符串也应该匹配所有
        assertTrue(alertSilenceService.matchCondition(
                "   ",
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "host-1",
                Collections.emptyList()
        ));
    }

    @Test
    void matchByEventType() throws Exception {
        AlertSilenceRecord.MatchCondition condition = new AlertSilenceRecord.MatchCondition(
                List.of(AlertEventType.HOST_OFFLINE),
                null,
                null,
                null,
                null
        );

        String conditionJson = new ObjectMapper().writeValueAsString(condition);

        // 匹配的事件类型
        boolean matched = alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                null,
                null
        );
        assertTrue(matched);

        // 不匹配的事件类型
        boolean notMatched = alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_RESOURCE_HIGH.name(),
                AlertLevel.ERROR.name(),
                null,
                null
        );
        assertFalse(notMatched);
    }

    @Test
    void matchByLevel() throws Exception {
        AlertSilenceRecord.MatchCondition condition = new AlertSilenceRecord.MatchCondition(
                null,
                List.of(AlertLevel.CRITICAL, AlertLevel.ERROR),
                null,
                null,
                null
        );

        String conditionJson = new ObjectMapper().writeValueAsString(condition);

        // 匹配的级别
        assertTrue(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                null,
                null
        ));

        assertTrue(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.CRITICAL.name(),
                null,
                null
        ));

        // 不匹配的级别
        assertFalse(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.WARNING.name(),
                null,
                null
        ));
    }

    @Test
    void matchBySource() throws Exception {
        AlertSilenceRecord.MatchCondition condition = new AlertSilenceRecord.MatchCondition(
                null,
                null,
                List.of("server-1", "server-2"),
                null,
                null
        );

        String conditionJson = new ObjectMapper().writeValueAsString(condition);

        // 匹配的来源
        assertTrue(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "server-1",
                null
        ));

        // 不匹配的来源
        assertFalse(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "server-3",
                null
        ));
    }

    @Test
    void matchByTags() throws Exception {
        AlertSilenceRecord.MatchCondition condition = new AlertSilenceRecord.MatchCondition(
                null,
                null,
                null,
                List.of("maintenance", "testing"),
                null
        );

        String conditionJson = new ObjectMapper().writeValueAsString(condition);

        // 匹配的标签
        assertTrue(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                null,
                List.of("maintenance", "production")
        ));

        // 不匹配的标签
        assertFalse(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                null,
                List.of("production", "critical")
        ));

        // 空标签
        assertFalse(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                null,
                null
        ));
    }

    @Test
    void matchMultipleConditions() throws Exception {
        AlertSilenceRecord.MatchCondition condition = new AlertSilenceRecord.MatchCondition(
                List.of(AlertEventType.HOST_OFFLINE),
                List.of(AlertLevel.ERROR, AlertLevel.CRITICAL),
                List.of("server-1"),
                null,
                null
        );

        String conditionJson = new ObjectMapper().writeValueAsString(condition);

        // 全部匹配
        assertTrue(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "server-1",
                null
        ));

        // 事件类型不匹配
        assertFalse(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_RESOURCE_HIGH.name(),
                AlertLevel.ERROR.name(),
                "server-1",
                null
        ));

        // 来源不匹配
        assertFalse(alertSilenceService.matchCondition(
                conditionJson,
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                "server-2",
                null
        ));
    }

    @Test
    void matchInvalidJsonCondition() {
        boolean result = alertSilenceService.matchCondition(
                "{ invalid json }",
                AlertEventType.HOST_OFFLINE.name(),
                AlertLevel.ERROR.name(),
                null,
                null
        );

        // 无效 JSON 返回 false
        assertFalse(result);
    }
}