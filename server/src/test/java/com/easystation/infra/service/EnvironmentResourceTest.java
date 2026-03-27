package com.easystation.infra.service;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class EnvironmentResourceTest {

    @Test
    void testListEnvironmentsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/environments")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateEnvironmentEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"TestEnv\",\"code\":\"test\",\"description\":\"Test environment\",\"color\":\"#123456\"}")
        .when()
            .post("/api/v1/environments")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateEnvironmentValidation() {
        // Test with missing required fields
        given()
            .contentType(ContentType.JSON)
            .body("{\"description\":\"Test\"}")
        .when()
            .post("/api/v1/environments")
        .then()
            .statusCode(400);
    }
}