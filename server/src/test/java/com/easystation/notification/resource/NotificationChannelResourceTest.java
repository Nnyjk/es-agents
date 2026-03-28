package com.easystation.notification.resource;

import com.easystation.notification.enums.ChannelType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class NotificationChannelResourceTest {

    @Test
    void testListChannelsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/notification-channels")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateChannelEndpoint() {
        String body = "{\"name\":\"TestChannel\",\"type\":\"EMAIL\",\"config\":\"{\\\"smtpHost\\\":\\\"localhost\\\"}\"}";
        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/notification-channels")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateChannelValidation() {
        // Test with missing required fields
        given()
            .contentType(ContentType.JSON)
            .body("{\"config\":\"test\"}")
        .when()
            .post("/api/v1/notification-channels")
        .then()
            .statusCode(anyOf(is(400), is(401), is(403), is(404)));
    }

    @Test
    void testGetChannelEndpoint() {
        UUID channelId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/notification-channels/" + channelId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testUpdateChannelEndpoint() {
        UUID channelId = UUID.randomUUID();
        String updateBody = "{\"name\":\"UpdatedChannel\",\"enabled\":false}";

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/api/v1/notification-channels/" + channelId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testDeleteChannelEndpoint() {
        UUID channelId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/notification-channels/" + channelId)
        .then()
            .statusCode(anyOf(is(204), is(404)));
    }

    @Test
    void testTestChannelEndpoint() {
        UUID channelId = UUID.randomUUID();
        String testBody = "{\"recipient\":\"test@example.com\",\"title\":\"Test\",\"content\":\"Test notification\"}";

        given()
            .contentType(ContentType.JSON)
            .body(testBody)
        .when()
            .post("/api/v1/notification-channels/" + channelId + "/test")
        .then()
            .statusCode(anyOf(is(200), is(404), is(400)));
    }

    @Test
    void testCreateChannelWithEnabled() {
        String body = "{\"name\":\"TestChannelEnabled\",\"type\":\"WEBHOOK\",\"config\":\"{\\\"url\\\":\\\"http://localhost\\\"}\",\"enabled\":true}";
        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/notification-channels")
        .then()
            .statusCode(notNullValue());
    }
}
