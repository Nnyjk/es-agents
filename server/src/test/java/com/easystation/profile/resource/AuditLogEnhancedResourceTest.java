package com.easystation.profile.resource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(AuditLogEnhancedResource.class)
public class AuditLogEnhancedResourceTest {

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testGetSummary() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "-7d")
            .queryParam("endTime", "now")
        .when()
            .get("/summary")
        .then()
            .statusCode(200)
            .body("userId", notNullValue())
            .body("summary", notNullValue())
            .body("summary.totalLogs", notNullValue())
            .body("summary.sensitiveLogs", notNullValue())
            .body("summary.failureLogs", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testExportCSV() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "-7d")
            .queryParam("endTime", "now")
            .queryParam("limit", 100)
        .when()
            .get("/export/csv")
        .then()
            .statusCode(200)
            .contentType("text/plain")  // CSV is returned as text/plain
            .body(containsString("ID,User ID,Action"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testExportJSON() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "-7d")
            .queryParam("endTime", "now")
            .queryParam("limit", 100)
        .when()
            .get("/export/json")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("logs", notNullValue())
            .body("count", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testExportGzip() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "-7d")
            .queryParam("endTime", "now")
            .queryParam("limit", 100)
        .when()
            .get("/export/gzip")
        .then()
            .statusCode(200)
            .contentType("application/gzip");
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testDetectAnomalies() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "-7d")
            .queryParam("endTime", "now")
        .when()
            .get("/anomalies")
        .then()
            .statusCode(200)
            .body("userId", notNullValue())
            .body("anomalyCount", notNullValue())
            .body("anomalies", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testGetByRiskLevel() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("startTime", "-7d")
            .queryParam("endTime", "now")
        .when()
            .get("/risk/LOW")
        .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testGetRequiresReview() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/requires-review")
        .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testVerifyLogIntegrity_NotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/non-existent-id/verify")
        .then()
            .statusCode(404)
            .body("error", equalTo("Log not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testReviewLog_NotFound() {
        Map<String, String> reviewRequest = new HashMap<>();
        reviewRequest.put("status", "REVIEWED");
        reviewRequest.put("notes", "Test review");

        given()
            .contentType(ContentType.JSON)
            .body(reviewRequest)
        .when()
            .post("/non-existent-id/review")
        .then()
            .statusCode(404)
            .body("error", equalTo("Log not found"));
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void testAccessDenied_ForNonAuditor() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/summary")
        .then()
            .statusCode(403);
    }
}
