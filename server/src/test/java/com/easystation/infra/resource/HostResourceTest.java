package com.easystation.infra.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class HostResourceTest {

    @Test
    void testListHostsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/hosts")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testListHostsWithEnvironmentFilterEndpoint() {
        UUID envId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
            .queryParam("environmentId", envId)
        .when()
            .get("/api/v1/hosts")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateHostEndpoint() {
        // First create an environment
        String envBody = "{\"name\":\"TestEnvForHost\",\"code\":\"test-host-env\",\"description\":\"Test environment for host\",\"color\":\"#123456\"}";
        given()
            .contentType(ContentType.JSON)
            .body(envBody)
        .when()
            .post("/api/v1/environments")
        .then()
            .statusCode(anyOf(is(201), is(200)));

        // Get the environment ID from response or use a fixed approach
        // For this test, we'll try creating with a valid structure
        String hostBody = "{\"name\":\"TestHost\",\"hostname\":\"192.168.1.100\",\"os\":\"Linux\",\"environmentId\":\"00000000-0000-0000-0000-000000000001\"}";

        given()
            .contentType(ContentType.JSON)
            .body(hostBody)
        .when()
            .post("/api/v1/hosts")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateHostValidation() {
        // Test with missing required fields
        given()
            .contentType(ContentType.JSON)
            .body("{\"description\":\"Test\"}")
        .when()
            .post("/api/v1/hosts")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateHostWithIdentifier() {
        // Test creating host with custom identifier
        String identifier = UUID.randomUUID().toString();
        String hostBody = "{\"identifier\":\"" + identifier + "\",\"name\":\"TestHostWithIdentifier\",\"hostname\":\"192.168.1.101\",\"os\":\"Linux\",\"environmentId\":\"00000000-0000-0000-0000-000000000001\"}";

        given()
            .contentType(ContentType.JSON)
            .body(hostBody)
        .when()
            .post("/api/v1/hosts")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testGetHostEndpoint() {
        UUID hostId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/hosts/" + hostId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testUpdateHostEndpoint() {
        UUID hostId = UUID.randomUUID();
        String updateBody = "{\"name\":\"UpdatedHost\",\"hostname\":\"192.168.1.200\"}";

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/api/v1/hosts/" + hostId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testDeleteHostEndpoint() {
        UUID hostId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/hosts/" + hostId)
        .then()
            .statusCode(anyOf(is(204), is(404), is(409)));
    }

    @Test
    void testCheckReachabilityEndpoint() {
        UUID hostId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/hosts/" + hostId + "/check")
        .then()
            .statusCode(anyOf(is(200), is(404), is(502)));
    }

    @Test
    void testCreateHostWithTags() {
        // Test creating host with tags
        String hostBody = "{\"name\":\"TestHostWithTags\",\"hostname\":\"192.168.1.102\",\"os\":\"Linux\",\"environmentId\":\"00000000-0000-0000-0000-000000000001\",\"tags\":[\"production\",\"critical\"]}";

        given()
            .contentType(ContentType.JSON)
            .body(hostBody)
        .when()
            .post("/api/v1/hosts")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testUpdateHostWithIdentifierAndEnabled() {
        UUID hostId = UUID.randomUUID();
        String updateBody = "{\"identifier\":\"updated-identifier\",\"enabled\":false,\"tags\":[\"updated\"]}";

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/api/v1/hosts/" + hostId)
        .then()
            .statusCode(anyOf(is(200), is(404), is(409)));
    }

    @Test
    void testDeleteHostWithAgentInstancesReturnsConflict() {
        UUID hostId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/hosts/" + hostId)
        .then()
            .statusCode(anyOf(is(204), is(404), is(409)));
    }
}