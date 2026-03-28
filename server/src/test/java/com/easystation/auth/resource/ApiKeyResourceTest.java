package com.easystation.auth.resource;

import com.easystation.auth.dto.ApiKeyRecord;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ApiKeyResourceTest {

    @Test
    void testListEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/api-keys")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testListWithFilters() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("enabled", true)
            .queryParam("limit", 10)
        .when()
            .get("/api/v1/api-keys")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testCreateEndpoint() {
        ApiKeyRecord.Create createDto = new ApiKeyRecord.Create(
                "Test API Key",
                "Test description",
                LocalDateTime.now().plusDays(30),
                Arrays.asList("agent:view", "host:view"),
                Arrays.asList("192.168.1.*"),
                UUID.randomUUID()
        );

        given()
            .contentType(ContentType.JSON)
            .body(createDto)
        .when()
            .post("/api/v1/api-keys")
        .then()
            .statusCode(anyOf(is(201), is(401), is(403)))
            .body("id", notNullValue())
            .body("name", equalTo("Test API Key"))
            .body("key", notNullValue());
    }

    @Test
    void testCreateWithMinimalFields() {
        ApiKeyRecord.Create createDto = new ApiKeyRecord.Create(
                "Minimal Key",
                null,
                null,
                null,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(createDto)
        .when()
            .post("/api/v1/api-keys")
        .then()
            .statusCode(anyOf(is(201), is(401), is(403)))
            .body("name", equalTo("Minimal Key"));
    }

    @Test
    void testValidateEndpointWithoutKey() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("key", "")
        .when()
            .post("/api/v1/api-keys/validate")
        .then()
            .statusCode(400)
            .body("valid", equalTo(false))
            .body("message", equalTo("API Key is required"));
    }

    @Test
    void testValidateEndpointWithInvalidKey() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("key", "esak_invalidkey123")
            .queryParam("clientIp", "192.168.1.100")
        .when()
            .post("/api/v1/api-keys/validate")
        .then()
            .statusCode(200)
            .body("valid", equalTo(false))
            .body("message", equalTo("API Key not found"));
    }

    @Test
    void testGetEndpointWithInvalidId() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/api-keys/" + UUID.randomUUID())
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testUpdateEndpointWithInvalidId() {
        ApiKeyRecord.Update updateDto = new ApiKeyRecord.Update(
                "Updated Name",
                null,
                null,
                null,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/api/v1/api-keys/" + UUID.randomUUID())
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testRevokeEndpointWithInvalidId() {
        ApiKeyRecord.RevokeRequest revokeDto = new ApiKeyRecord.RevokeRequest(
                UUID.randomUUID(),
                "Security concern"
        );

        given()
            .contentType(ContentType.JSON)
            .body(revokeDto)
        .when()
            .post("/api/v1/api-keys/" + UUID.randomUUID() + "/revoke")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testRefreshEndpointWithInvalidId() {
        ApiKeyRecord.RefreshRequest refreshDto = new ApiKeyRecord.RefreshRequest(
                UUID.randomUUID()
        );

        given()
            .contentType(ContentType.JSON)
            .body(refreshDto)
        .when()
            .post("/api/v1/api-keys/" + UUID.randomUUID() + "/refresh")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404), is(400)));
    }

    @Test
    void testDeleteEndpointWithInvalidId() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/api-keys/" + UUID.randomUUID())
        .then()
            .statusCode(anyOf(is(204), is(401), is(403), is(404)));
    }

    @Test
    void testUsageLogsEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("limit", 50)
        .when()
            .get("/api/v1/api-keys/logs")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testUsageLogsForSpecificKeyEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("limit", 50)
        .when()
            .get("/api/v1/api-keys/" + UUID.randomUUID() + "/logs")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testUpdateEnabled() {
        ApiKeyRecord.Update updateDto = new ApiKeyRecord.Update(
                null,
                null,
                null,
                false,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/api/v1/api-keys/" + UUID.randomUUID())
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testUpdatePermissions() {
        ApiKeyRecord.Update updateDto = new ApiKeyRecord.Update(
                null,
                null,
                null,
                null,
                Arrays.asList("agent:*", "host:view", "config:view"),
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/api/v1/api-keys/" + UUID.randomUUID())
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testUpdateIpWhitelist() {
        ApiKeyRecord.Update updateDto = new ApiKeyRecord.Update(
                null,
                null,
                null,
                null,
                null,
                Arrays.asList("192.168.1.100", "10.0.0.*")
        );

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .put("/api/v1/api-keys/" + UUID.randomUUID())
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testCreateWithAllFields() {
        ApiKeyRecord.Create createDto = new ApiKeyRecord.Create(
                "Full API Key",
                "Complete test key",
                LocalDateTime.now().plusDays(90),
                Arrays.asList("*"),
                Arrays.asList("192.168.*.*", "10.*.*.*"),
                UUID.randomUUID()
        );

        given()
            .contentType(ContentType.JSON)
            .body(createDto)
        .when()
            .post("/api/v1/api-keys")
        .then()
            .statusCode(anyOf(is(201), is(401), is(403)))
            .body("name", equalTo("Full API Key"))
            .body("enabled", equalTo(true));
    }

    @Test
    void testValidateWithPermission() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("key", "esak_testkey")
            .queryParam("clientIp", "192.168.1.100")
            .queryParam("permission", "agent:view")
        .when()
            .post("/api/v1/api-keys/validate")
        .then()
            .statusCode(200)
            .body("valid", equalTo(false));
    }

    @Test
    void testCreateWithoutNameFails() {
        ApiKeyRecord.Create createDto = new ApiKeyRecord.Create(
                null,
                "Test description",
                null,
                null,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(createDto)
        .when()
            .post("/api/v1/api-keys")
        .then()
            .statusCode(400);
    }
}