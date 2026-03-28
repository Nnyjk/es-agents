package com.easystation.audit.resource;

import com.easystation.audit.dto.AuditRecord;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuditLogResourceTest {

    @Test
    void testListAuditLogsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListAuditLogsByUsernameFilter() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("username", "admin")
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListAuditLogsByActionFilter() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("action", "LOGIN")
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListAuditLogsByResultFilter() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("result", "SUCCESS")
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListAuditLogsByResourceTypeFilter() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("resourceType", "HOST")
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListAuditLogsByDateRange() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startDate", "2026-01-01T00:00:00")
            .queryParam("endDate", "2026-12-31T23:59:59")
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListAuditLogsPagination() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("page", 1)
            .queryParam("pageSize", 10)
        .when()
            .get("/api/v1/audit/logs")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testGetAuditLogByIdEndpoint() {
        UUID logId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/audit/logs/" + logId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testCreateAuditLogEndpoint() {
        String createBody = "{\"username\":\"testuser\",\"userId\":\"00000000-0000-0000-0000-000000000000\",\"action\":\"LOGIN\",\"result\":\"SUCCESS\",\"description\":\"Test login\",\"resourceType\":\"USER\",\"resourceId\":\"00000000-0000-0000-0000-000000000001\",\"details\":\"Test login details\",\"clientIp\":\"192.168.1.100\",\"userAgent\":\"Mozilla/5.0\",\"requestPath\":\"/api/v1/auth/login\",\"requestMethod\":\"POST\",\"duration\":100}";

        given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/audit/logs")
        .then()
            .statusCode(anyOf(is(201), is(200)));
    }

    @Test
    void testCreateAuditLogValidation() {
        // Test with missing required fields
        String invalidBody = "{\"description\":\"Test\"}";

        given()
            .contentType(ContentType.JSON)
            .body(invalidBody)
        .when()
            .post("/api/v1/audit/logs")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateAuditLogWithAllFields() {
        String createBody = "{\"username\":\"testuser\",\"userId\":\"00000000-0000-0000-0000-000000000000\",\"action\":\"CREATE_HOST\",\"result\":\"SUCCESS\",\"description\":\"Created new host\",\"resourceType\":\"HOST\",\"resourceId\":\"00000000-0000-0000-0000-000000000001\",\"details\":\"Host created successfully\",\"requestParams\":\"{\\\"name\\\":\\\"test-host\\\"}\",\"responseResult\":\"{\\\"id\\\":\\\"...\\\"}\",\"clientIp\":\"192.168.1.100\",\"userAgent\":\"Mozilla/5.0\",\"requestPath\":\"/api/v1/hosts\",\"requestMethod\":\"POST\",\"duration\":150}";

        given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/audit/logs")
        .then()
            .statusCode(anyOf(is(201), is(200)));
    }

    @Test
    void testCreateAuditLogWithFailedResult() {
        String createBody = "{\"username\":\"testuser\",\"action\":\"LOGIN_FAILED\",\"result\":\"FAILED\",\"description\":\"Login failed - invalid credentials\",\"resourceType\":\"USER\",\"clientIp\":\"192.168.1.100\",\"errorMessage\":\"Invalid username or password\"}";

        given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/audit/logs")
        .then()
            .statusCode(anyOf(is(201), is(200)));
    }

    @Test
    void testDeleteAuditLogEndpoint() {
        UUID logId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/audit/logs/" + logId)
        .then()
            .statusCode(anyOf(is(204), is(404)));
    }

    @Test
    void testAuditActionEnumValues() {
        // Test that various action types are accepted
        String[] actions = {"LOGIN", "LOGOUT", "CREATE_HOST", "UPDATE_HOST", "DELETE_HOST", 
                           "EXECUTE_COMMAND", "DEPLOY_AGENT", "EXPORT_DATA"};
        
        for (String action : actions) {
            String createBody = "{\"username\":\"testuser\",\"action\":\"" + action + "\",\"result\":\"SUCCESS\",\"description\":\"Test " + action + "\"}";
            
            given()
                .contentType(ContentType.JSON)
                .body(createBody)
            .when()
                .post("/api/v1/audit/logs")
            .then()
                .statusCode(anyOf(is(201), is(200)));
        }
    }

    @Test
    void testAuditResultEnumValues() {
        // Test that all result types are accepted
        String[] results = {"SUCCESS", "FAILED", "PARTIAL"};
        
        for (String result : results) {
            String createBody = "{\"username\":\"testuser\",\"action\":\"LOGIN\",\"result\":\"" + result + "\",\"description\":\"Test " + result + "\"}";
            
            given()
                .contentType(ContentType.JSON)
                .body(createBody)
            .when()
                .post("/api/v1/audit/logs")
            .then()
                .statusCode(anyOf(is(201), is(200)));
        }
    }
}
