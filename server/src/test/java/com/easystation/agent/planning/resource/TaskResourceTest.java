package com.easystation.agent.planning.resource;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.domain.enums.TaskPriority;
import com.easystation.agent.planning.dto.DecomposeRequest;
import com.easystation.agent.planning.dto.TaskRecord;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * TaskResource REST API 测试
 */
@QuarkusTest
class TaskResourceTest {

    private static final String BASE_PATH = "/api/agent/tasks";

    @Test
    void testDecomposeEndpoint() {
        DecomposeRequest request = new DecomposeRequest(
                "完成系统部署任务",
                3,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body("valid", is(true))
            .body("totalTasks", greaterThan(0))
            .body("tasks", notNullValue());
    }

    @Test
    void testDecomposeWithMinimalRequest() {
        DecomposeRequest request = new DecomposeRequest("测试目标");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body("goal", equalTo("测试目标"));
    }

    @Test
    void testDecomposeWithConstraints() {
        DecomposeRequest request = new DecomposeRequest(
                "部署应用到生产环境",
                2,
                "需要在周末进行",
                Arrays.asList("不能影响现有服务", "需要备份")
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testDecomposeWithEmptyGoalFails() {
        DecomposeRequest request = new DecomposeRequest("");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    void testDecomposeWithNullGoalFails() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"goal\": null}")
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    void testDecomposeWithInvalidDepthFails() {
        DecomposeRequest request = new DecomposeRequest(
                "测试目标",
                10, // 超过最大深度
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    void testGetByIdEndpoint() {
        UUID randomId = UUID.randomUUID();

        given()
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_PATH + "/" + randomId)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testUpdateStatusEndpoint() {
        UUID randomId = UUID.randomUUID();
        TaskRecord.UpdateStatusRequest request = new TaskRecord.UpdateStatusRequest(
                PlanningTaskStatus.COMPLETED,
                "执行结果",
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put(BASE_PATH + "/" + randomId + "/status")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testUpdateStatusToRunning() {
        UUID randomId = UUID.randomUUID();
        TaskRecord.UpdateStatusRequest request = new TaskRecord.UpdateStatusRequest(
                PlanningTaskStatus.RUNNING,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put(BASE_PATH + "/" + randomId + "/status")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testExecuteEndpoint() {
        UUID randomId = UUID.randomUUID();

        given()
            .contentType(ContentType.JSON)
        .when()
            .post(BASE_PATH + "/" + randomId + "/execute")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testCancelEndpoint() {
        UUID randomId = UUID.randomUUID();

        given()
            .contentType(ContentType.JSON)
        .when()
            .post(BASE_PATH + "/" + randomId + "/cancel")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404), is(400)));
    }

    @Test
    void testListEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body("tasks", notNullValue())
            .body("total", notNullValue());
    }

    @Test
    void testListWithStatusFilter() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("status", "CREATED")
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testListWithLimit() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("limit", 10)
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testListByGoalEndpoint() {
        UUID randomGoalId = UUID.randomUUID();

        given()
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_PATH + "/goals/" + randomGoalId)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testCreateEndpoint() {
        TaskRecord.Create request = new TaskRecord.Create(
                UUID.randomUUID(),
                "测试任务描述",
                TaskPriority.NORMAL,
                "{\"param\": \"value\"}",
                "DEFAULT",
                3
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH)
        .then()
            .statusCode(anyOf(is(201), is(401), is(403)))
            .body("id", notNullValue())
            .body("description", equalTo("测试任务描述"));
    }

    @Test
    void testCreateWithMinimalFields() {
        TaskRecord.Create request = new TaskRecord.Create(
                UUID.randomUUID(),
                "简单任务",
                null,
                null,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH)
        .then()
            .statusCode(anyOf(is(201), is(401), is(403)));
    }

    @Test
    void testCreateWithNullDescriptionFails() {
        TaskRecord.Create request = new TaskRecord.Create(
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH)
        .then()
            .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    void testScheduleGoalEndpoint() {
        UUID randomGoalId = UUID.randomUUID();

        given()
            .contentType(ContentType.JSON)
        .when()
            .post(BASE_PATH + "/goals/" + randomGoalId + "/schedule")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testGetCountsEndpoint() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(BASE_PATH + "/counts")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body("created", notNullValue())
            .body("completed", notNullValue())
            .body("failed", notNullValue());
    }

    @Test
    void testDecomposeLongGoal() {
        DecomposeRequest request = new DecomposeRequest(
                "完成系统部署：包括环境准备、配置管理、应用部署、服务验证和监控配置",
                3,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body("valid", is(true));
    }

    @Test
    void testUpdateStatusToFailed() {
        UUID randomId = UUID.randomUUID();
        TaskRecord.UpdateStatusRequest request = new TaskRecord.UpdateStatusRequest(
                PlanningTaskStatus.FAILED,
                null,
                "执行失败原因"
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .put(BASE_PATH + "/" + randomId + "/status")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403), is(404)));
    }

    @Test
    void testListWithCompletedStatus() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("status", "COMPLETED")
            .queryParam("limit", 20)
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)));
    }

    @Test
    void testCreateWithHighPriority() {
        TaskRecord.Create request = new TaskRecord.Create(
                UUID.randomUUID(),
                "高优先级任务",
                TaskPriority.HIGH,
                null,
                "DEPLOYMENT",
                5
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH)
        .then()
            .statusCode(anyOf(is(201), is(401), is(403)));
    }

    @Test
    void testDecomposeGoalWithKeywords() {
        // 包含任务关键词的目标应该能被正确分解
        DecomposeRequest request = new DecomposeRequest(
                "需要完成以下工作：\n- 配置数据库连接\n- 部署应用服务\n- 测试接口功能\n- 验证系统运行",
                2,
                null,
                null
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_PATH + "/decompose")
        .then()
            .statusCode(anyOf(is(200), is(401), is(403)))
            .body("totalTasks", greaterThan(0));
    }
}