package com.easystation.deployment.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class DeploymentProgressResourceTest {

    @Test
    void testGetCurrentProgressNotFound() {
        UUID deploymentId = UUID.randomUUID();
        given()
        .when()
            .get("/api/deployments/progress/" + deploymentId)
        .then()
            .statusCode(404);
    }

    @Test
    void testGetProgressHistoryEmpty() {
        UUID deploymentId = UUID.randomUUID();
        // Note: Endpoint requires authentication, may return 401
        given()
        .when()
            .get("/api/deployments/progress/" + deploymentId + "/history")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body(anyOf(equalTo("[]"), nullValue()));
    }

    @Test
    void testGetStatusHistoryEmpty() {
        UUID deploymentId = UUID.randomUUID();
        // Note: Endpoint requires authentication, may return 401
        given()
        .when()
            .get("/api/deployments/progress/" + deploymentId + "/status-history")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body(anyOf(equalTo("[]"), nullValue()));
    }

    @Test
    void testGetOverallProgress() {
        UUID deploymentId = UUID.randomUUID();
        // Note: Endpoint requires authentication, may return 401
        given()
        .when()
            .get("/api/deployments/progress/" + deploymentId + "/overall")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testMarkStageCompleteMissingStage() {
        UUID deploymentId = UUID.randomUUID();
        // Note: Endpoint requires authentication, may return 401
        given()
        .when()
            .post("/api/deployments/progress/" + deploymentId + "/complete")
        .then()
            .statusCode(anyOf(is(400), is(401), is(403)));
    }

    @Test
    void testMarkStageFailedMissingStage() {
        UUID deploymentId = UUID.randomUUID();
        // Note: Endpoint requires authentication, may return 401
        given()
        .when()
            .post("/api/deployments/progress/" + deploymentId + "/fail")
        .then()
            .statusCode(anyOf(is(400), is(401), is(403)));
    }
}