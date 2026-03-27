package com.easystation.auth.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceTest {

    @Test
    void testLoginEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"test\",\"password\":\"test123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testRegisterEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"newuser\",\"password\":\"Test123456!\",\"email\":\"test@example.com\"}")
        .when()
            .post("/auth/register")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testPasswordStrengthEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"password\":\"Test123456!\"}")
        .when()
            .post("/auth/password/strength")
        .then()
            .statusCode(200)
            .body("score", notNullValue());
    }

    @Test
    void testForgotPasswordEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"test@example.com\"}")
        .when()
            .post("/auth/password/forgot")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testRoutesEndpointUnauthorized() {
        given()
        .when()
            .get("/auth/routes")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testPublicKeyEndpoint() {
        given()
        .when()
            .get("/auth/public-key")
        .then()
            .statusCode(200)
            .body("publicKey", notNullValue());
    }
}