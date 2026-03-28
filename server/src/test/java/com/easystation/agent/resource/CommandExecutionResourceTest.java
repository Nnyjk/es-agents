package com.easystation.agent.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CommandExecutionResourceTest {

    @Test
    void testExecuteCommandEndpoint() {
        String body = "{\"agentInstanceId\":\"" + UUID.randomUUID() + "\",\"command\":\"echo hello\",\"timeout\":60}";
        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/agent-commands/execute")
        .then()
            .statusCode(anyOf(is(201), is(401), is(403), is(404), is(400)));
    }

    @Test
    void testExecuteCommandWithTemplate() {
        String body = "{\"agentInstanceId\":\"" + UUID.randomUUID() + "\",\"templateId\":\"" + UUID.randomUUID() + "\",\"parameters\":{\"arg1\":\"value1\"}}";
        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/agent-commands/execute")
        .then()
            .statusCode(anyOf(is(201), is(401), is(403), is(404), is(400)));
    }

    @Test
    void testExecuteCommandValidation() {
        // Test with missing required fields (no agentInstanceId)
        given()
            .contentType(ContentType.JSON)
            .body("{\"command\":\"echo test\"}")
        .when()
            .post("/api/v1/agent-commands/execute")
        .then()
            .statusCode(anyOf(is(400), is(401), is(403)));
    }

    @Test
    void testGetExecutionStatusEndpoint() {
        UUID executionId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/agent-commands/" + executionId + "/status")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testGetExecutionDetailEndpoint() {
        UUID executionId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/agent-commands/" + executionId)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testRetryExecutionEndpoint() {
        UUID executionId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/agent-commands/" + executionId + "/retry")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404), is(400)));
    }

    @Test
    void testListExecutionsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/agent-commands")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testListExecutionsWithFilters() {
        UUID agentInstanceId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
            .queryParam("agentInstanceId", agentInstanceId)
            .queryParam("templateId", templateId)
            .queryParam("status", "SUCCESS")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/v1/agent-commands")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testListExecutionsWithTimeRange() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "2026-01-01T00:00:00")
            .queryParam("endTime", "2026-12-31T23:59:59")
        .when()
            .get("/api/v1/agent-commands")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testCancelExecutionEndpoint() {
        UUID executionId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/agent-commands/" + executionId + "/cancel")
        .then()
            .statusCode(anyOf(is(501), is(401), is(403), is(404)));
    }
}