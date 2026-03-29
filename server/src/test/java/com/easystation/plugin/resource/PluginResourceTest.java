package com.easystation.plugin.resource;

import com.easystation.plugin.domain.enums.PluginStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class PluginResourceTest {

    @Test
    void testCreatePluginEndpoint() {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "test-plugin-" + System.currentTimeMillis());
        body.put("name", "测试插件");
        body.put("description", "这是一个测试插件");
        body.put("categoryId", UUID.randomUUID().toString());
        body.put("developerId", UUID.randomUUID().toString());
        body.put("developerName", "test-developer");
        body.put("initialVersion", "1.0.0");
        body.put("iconUrl", "https://example.com/icon.png");
        body.put("tags", List.of("测试", "工具"));

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("code", startsWith("test-plugin-"))
            .body("name", equalTo("测试插件"))
            .body("status", equalTo("DRAFT"));
    }

    @Test
    void testListPluginsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/plugins")
        .then()
            .statusCode(200)
            .body("", isA(List.class));
    }

    @Test
    void testGetPluginEndpoint() {
        // First create a plugin to get its ID
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "get-plugin-test");
        createBody.put("name", "获取插件测试");
        createBody.put("description", "用于测试获取插件功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then get the plugin
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", pluginId)
        .when()
            .get("/api/v1/plugins/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(pluginId))
            .body("code", equalTo("get-plugin-test"));
    }

    @Test
    void testUpdatePluginEndpoint() {
        // First create a plugin
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "update-plugin-test");
        createBody.put("name", "更新前名称");
        createBody.put("description", "更新前描述");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Update the plugin
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("name", "更新后名称");
        updateBody.put("description", "更新后描述");
        updateBody.put("iconUrl", "https://example.com/new-icon.png");
        updateBody.put("tags", List.of("更新", "测试"));

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", pluginId)
            .body(updateBody)
        .when()
            .put("/api/v1/plugins/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(pluginId))
            .body("name", equalTo("更新后名称"))
            .body("description", equalTo("更新后描述"))
            .body("iconUrl", equalTo("https://example.com/new-icon.png"));
    }

    @Test
    void testDeletePluginEndpoint() {
        // First create a plugin
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "delete-plugin-test");
        createBody.put("name", "删除插件测试");
        createBody.put("description", "用于测试删除插件功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Delete the plugin
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", pluginId)
        .when()
            .delete("/api/v1/plugins/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(pluginId))
            .body("status", equalTo("DELETED"));
    }

    @Test
    void testSearchPluginsEndpoint() {
        // Create test plugins
        Map<String, Object> createBody1 = new HashMap<>();
        createBody1.put("code", "search-test-1");
        createBody1.put("name", "搜索测试插件一");
        createBody1.put("description", "用于测试搜索功能");
        createBody1.put("categoryId", UUID.randomUUID().toString());
        createBody1.put("developerId", UUID.randomUUID().toString());
        createBody1.put("developerName", "test-developer");
        createBody1.put("initialVersion", "1.0.0");
        createBody1.put("iconUrl", null);
        createBody1.put("tags", List.of("搜索", "测试"));
        given().contentType(ContentType.JSON).body(createBody1).post("/api/v1/plugins");

        Map<String, Object> createBody2 = new HashMap<>();
        createBody2.put("code", "search-test-2");
        createBody2.put("name", "搜索测试插件二");
        createBody2.put("description", "用于测试搜索功能");
        createBody2.put("categoryId", UUID.randomUUID().toString());
        createBody2.put("developerId", UUID.randomUUID().toString());
        createBody2.put("developerName", "test-developer");
        createBody2.put("initialVersion", "1.0.0");
        createBody2.put("iconUrl", null);
        createBody2.put("tags", List.of("搜索", "工具"));
        given().contentType(ContentType.JSON).body(createBody2).post("/api/v1/plugins");

        // Search plugins
        Map<String, Object> searchBody = new HashMap<>();
        searchBody.put("keyword", "搜索");

        given()
            .contentType(ContentType.JSON)
            .body(searchBody)
        .when()
            .post("/api/v1/plugins/search")
        .then()
            .statusCode(200)
            .body("", isA(List.class));
    }

    @Test
    void testCreateVersionEndpoint() {
        // First create a plugin
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "version-test-" + System.currentTimeMillis());
        createBody.put("name", "版本测试");
        createBody.put("description", "用于测试版本功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Create a version
        Map<String, Object> versionBody = new HashMap<>();
        versionBody.put("pluginId", pluginId);
        versionBody.put("version", "1.0.0");
        versionBody.put("releaseNotes", "初始版本");
        versionBody.put("downloadUrl", "https://example.com/download/v1.0.0.zip");
        versionBody.put("checksum", "SHA256_HASH_VALUE");
        versionBody.put("fileSize", 1024000);
        versionBody.put("releaseTime", java.time.LocalDateTime.now().toString());

        given()
            .contentType(ContentType.JSON)
            .body(versionBody)
        .when()
            .post("/api/v1/plugins/{pluginId}/versions", pluginId)
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("version", equalTo("1.0.0"))
            .body("pluginId", equalTo(pluginId));
    }

    @Test
    void testListVersionsEndpoint() {
        // First create a plugin
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "list-versions-test");
        createBody.put("name", "版本列表测试");
        createBody.put("description", "用于测试版本列表功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // List versions
        given()
            .contentType(ContentType.JSON)
            .pathParam("pluginId", pluginId)
        .when()
            .get("/api/v1/plugins/{pluginId}/versions")
        .then()
            .statusCode(200)
            .body("", isA(List.class));
    }

    @Test
    void testGetLatestVersionEndpoint() {
        // First create a plugin
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "latest-version-test");
        createBody.put("name", "最新版本测试");
        createBody.put("description", "用于测试最新版本功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Get latest version
        given()
            .contentType(ContentType.JSON)
            .pathParam("pluginId", pluginId)
        .when()
            .get("/api/v1/plugins/{pluginId}/versions/latest")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    void testGetVersionByIdEndpoint() {
        // First create a plugin and version
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "get-version-test");
        createBody.put("name", "获取版本测试");
        createBody.put("description", "用于测试获取版本功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        Map<String, Object> versionBody = new HashMap<>();
        versionBody.put("pluginId", pluginId);
        versionBody.put("version", "1.0.0");
        versionBody.put("releaseNotes", "初始版本");
        versionBody.put("downloadUrl", "https://example.com/download/v1.0.0.zip");
        versionBody.put("checksum", "SHA256_HASH_VALUE");
        versionBody.put("fileSize", 1024000);
        versionBody.put("releaseTime", java.time.LocalDateTime.now().toString());

        String versionId = given()
            .contentType(ContentType.JSON)
            .body(versionBody)
        .when()
            .post("/api/v1/plugins/{pluginId}/versions", pluginId)
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Get version by ID
        given()
            .contentType(ContentType.JSON)
            .pathParam("versionId", versionId)
        .when()
            .get("/api/v1/plugins/versions/{versionId}")
        .then()
            .statusCode(200)
            .body("id", equalTo(versionId))
            .body("version", equalTo("1.0.0"));
    }

    @Test
    void testUpdateVersionEndpoint() {
        // First create a plugin and version
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("code", "update-version-test");
        createBody.put("name", "更新版本测试");
        createBody.put("description", "用于测试更新版本功能");
        createBody.put("categoryId", UUID.randomUUID().toString());
        createBody.put("developerId", UUID.randomUUID().toString());
        createBody.put("developerName", "test-developer");
        createBody.put("initialVersion", "1.0.0");
        createBody.put("iconUrl", null);
        createBody.put("tags", List.of());

        String pluginId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/v1/plugins")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        Map<String, Object> versionBody = new HashMap<>();
        versionBody.put("pluginId", pluginId);
        versionBody.put("version", "1.0.0");
        versionBody.put("releaseNotes", "初始版本");
        versionBody.put("downloadUrl", "https://example.com/download/v1.0.0.zip");
        versionBody.put("checksum", "SHA256_HASH_VALUE");
        versionBody.put("fileSize", 1024000);
        versionBody.put("releaseTime", java.time.LocalDateTime.now().toString());

        String versionId = given()
            .contentType(ContentType.JSON)
            .body(versionBody)
        .when()
            .post("/api/v1/plugins/{pluginId}/versions", pluginId)
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Update version
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("releaseNotes", "更新后的发布说明");
        updateBody.put("downloadUrl", "https://example.com/download/v1.0.0-updated.zip");

        given()
            .contentType(ContentType.JSON)
            .pathParam("versionId", versionId)
            .body(updateBody)
        .when()
            .put("/api/v1/plugins/versions/{versionId}")
        .then()
            .statusCode(200)
            .body("id", equalTo(versionId))
            .body("releaseNotes", equalTo("更新后的发布说明"));
    }
}
