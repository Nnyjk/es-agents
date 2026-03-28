package com.easystation.agent.resource;

import com.easystation.agent.dto.SystemEventLogDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * 系统事件日志 API 测试
 */
@QuarkusTest
class SystemEventLogResourceTest {

    @Test
    void testQueryEventsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/v1/system-event-logs")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testQueryEventsWithFilters() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("eventType", "DEPLOY")
            .queryParam("eventLevel", "INFO")
            .queryParam("page", 0)
            .queryParam("size", 20)
        .when()
            .get("/v1/system-event-logs")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testGetEventById() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 1L)
        .when()
            .get("/v1/system-event-logs/{id}")
        .then()
            .statusCode(anyOf(is(200), is(404), is(401), is(403)));
    }

    @Test
    void testCleanupOldLogs() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("retentionDays", 30)
        .when()
            .delete("/v1/system-event-logs")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testLogEvent() {
        SystemEventLogDTO dto = new SystemEventLogDTO(
            null, "TEST_EVENT", "INFO", "test", "TEST", null, null,
            null, "测试事件", null, null, null, null, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/v1/system-event-logs")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }
}
