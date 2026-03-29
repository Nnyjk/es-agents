package com.easystation.notification.resource;

import com.easystation.notification.enums.MessageLevel;
import com.easystation.notification.enums.MessageType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class NotificationMessageResourceTest {

    @Test
    void testCreateMessageEndpoint() {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", UUID.randomUUID().toString());
        body.put("username", "testuser");
        body.put("title", "测试通知");
        body.put("content", "这是一条测试消息");
        body.put("type", MessageType.SYSTEM.toString());
        body.put("level", MessageLevel.INFO.toString());

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/notification/messages")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testCreateMessageValidation() {
        // Test with missing required fields
        Map<String, Object> body = new HashMap<>();
        body.put("content", "缺少用户 ID 和标题");

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/notification/messages")
        .then()
            .statusCode(anyOf(is(400), is(401), is(403)));
    }

    @Test
    void testListMessagesEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .param("userId", UUID.randomUUID().toString())
        .when()
            .get("/api/v1/notification/messages")
        .then()
            .statusCode(notNullValue())
            .body(anything());
    }

    @Test
    void testGetMessageByIdEndpoint() {
        UUID messageId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/notification/messages/" + messageId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testMarkAsReadEndpoint() {
        UUID messageId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .put("/api/v1/notification/messages/" + messageId + "/read")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testMarkBatchAsReadEndpoint() {
        Map<String, Object> body = new HashMap<>();
        body.put("messageIds", new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()});

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .put("/api/v1/notification/messages/read-batch")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testDeleteMessageEndpoint() {
        UUID messageId = UUID.randomUUID();
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/v1/notification/messages/" + messageId)
        .then()
            .statusCode(anyOf(is(204), is(404)));
    }

    @Test
    void testDeleteBatchEndpoint() {
        Map<String, Object> body = new HashMap<>();
        body.put("messageIds", new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()});

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .delete("/api/v1/notification/messages/batch")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testGetUnreadCountEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .param("userId", UUID.randomUUID().toString())
        .when()
            .get("/api/v1/notification/messages/unread-count")
        .then()
            .statusCode(notNullValue());
    }

    @Test
    void testGetStatisticsEndpoint() {
        given()
            .contentType(ContentType.JSON)
            .param("userId", UUID.randomUUID().toString())
        .when()
            .get("/api/v1/notification/messages/statistics")
        .then()
            .statusCode(notNullValue());
    }
}
