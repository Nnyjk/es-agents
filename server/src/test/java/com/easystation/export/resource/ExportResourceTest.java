package com.easystation.export.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ExportResourceTest {

    @Test
    void testCreateExportTask() {
        String requestBody = """
            {
                "exportType": "EXCEL",
                "dataType": "DEPLOYMENT_HISTORY",
                "startTime": null,
                "endTime": null,
                "keyword": null,
                "status": null,
                "action": null,
                "resourceType": null,
                "limit": 1000,
                "offset": 0
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/export")
        .then()
            .statusCode(anyOf(is(201), is(200)))
            .body("taskId", notNullValue());
    }

    @Test
    void testCreateExportTaskWithPdfType() {
        String requestBody = """
            {
                "exportType": "PDF",
                "dataType": "COMMAND_LOG",
                "startTime": null,
                "endTime": null,
                "keyword": null,
                "status": null,
                "action": null,
                "resourceType": null,
                "limit": 1000,
                "offset": 0
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/v1/export")
        .then()
            .statusCode(anyOf(is(201), is(200)))
            .body("taskId", notNullValue());
    }

    @Test
    void testListExportTasks() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/export/tasks")
        .then()
            .statusCode(notNullValue())
            .body("tasks", notNullValue());
    }

    @Test
    void testGetExportTaskById() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/export/tasks/{id}", "00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void testDownloadExportFile() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/v1/export/tasks/{id}/download", "00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }
}
