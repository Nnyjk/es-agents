package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.DeploymentProgressDTO;
import com.easystation.deployment.dto.DeploymentProgressHistoryDTO;
import com.easystation.deployment.service.DeploymentProgressService;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 部署进展资源
 */
@Path("/api/deployments/progress")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "部署进展管理", description = "部署进展跟踪管理 API")
public class DeploymentProgressResource {

    @Inject
    DeploymentProgressService progressService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    /**
     * 获取当前进展
     * GET /api/deployments/progress/{deploymentId}
     */
    @GET
    @Operation(summary = "获取当前进展", description = "获取部署任务的当前进展状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回当前进展"),
        @ApiResponse(responseCode = "404", description = "部署任务不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @Path("/{deploymentId}")
    @RequiresPermission("deployment:view")
    public Response getCurrentProgress(
            @Parameter(description = "部署任务 ID", in = ParameterIn.PATH) @PathParam("deploymentId") UUID deploymentId) {
        DeploymentProgressDTO progress = progressService.getCurrentProgress(deploymentId);
        if (progress == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(progress).build();
    }

    /**
     * 获取进展历史
     * GET /api/deployments/progress/{deploymentId}/history
     */
    @GET
    @Operation(summary = "获取进展历史", description = "获取部署任务的进展历史记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回进展历史列表"),
        @ApiResponse(responseCode = "404", description = "部署任务不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @Path("/{deploymentId}/history")
    @RequiresPermission("deployment:view")
    public Response getProgressHistory(
            @Parameter(description = "部署任务 ID", in = ParameterIn.PATH) @PathParam("deploymentId") UUID deploymentId) {
        List<DeploymentProgressDTO> history = progressService.getProgressHistory(deploymentId);
        return Response.ok(history).build();
    }

    /**
     * 获取状态变更历史
     * GET /api/deployments/progress/{deploymentId}/status-history
     */
    @GET
    @Operation(summary = "获取状态变更历史", description = "获取部署任务的状态变更历史记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回状态变更历史列表"),
        @ApiResponse(responseCode = "404", description = "部署任务不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @Path("/{deploymentId}/status-history")
    @RequiresPermission("deployment:view")
    public Response getStatusHistory(
            @Parameter(description = "部署任务 ID", in = ParameterIn.PATH) @PathParam("deploymentId") UUID deploymentId) {
        List<DeploymentProgressHistoryDTO> statusHistory = progressService.getStatusHistory(deploymentId);
        return Response.ok(statusHistory).build();
    }

    /**
     * 获取总体进度百分比
     * GET /api/deployments/progress/{deploymentId}/overall
     */
    @GET
    @Operation(summary = "获取总体进度", description = "获取部署任务的总体进度百分比")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回总体进度"),
        @ApiResponse(responseCode = "404", description = "部署任务不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @Path("/{deploymentId}/overall")
    @RequiresPermission("deployment:view")
    public Response getOverallProgress(
            @Parameter(description = "部署任务 ID", in = ParameterIn.PATH) @PathParam("deploymentId") UUID deploymentId) {
        int overallProgress = progressService.calculateOverallProgress(deploymentId);
        return Response.ok(Map.of("deploymentId", deploymentId, "overallProgress", overallProgress)).build();
    }

    /**
     * 标记阶段完成
     * POST /api/deployments/progress/{deploymentId}/complete
     */
    @POST
    @Operation(summary = "标记阶段完成", description = "标记部署任务的某个阶段为完成状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功标记阶段完成"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "部署任务不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @Path("/{deploymentId}/complete")
    @RequiresPermission("deployment:edit")
    public Response markStageComplete(
            @Parameter(description = "部署任务 ID", in = ParameterIn.PATH) @PathParam("deploymentId") UUID deploymentId,
            @Parameter(description = "阶段名称", in = ParameterIn.QUERY) @QueryParam("stage") String stage,
            @Parameter(description = "备注信息", in = ParameterIn.QUERY) @QueryParam("message") String message) {
        if (stage == null || stage.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "stage is required"))
                .build();
        }
        DeploymentProgressDTO progress = progressService.markStageComplete(deploymentId, stage, message);
        recordAuditLog(AuditAction.MARK_STAGE_COMPLETE, AuditResult.SUCCESS,
                "标记阶段完成：" + stage, "DeploymentProgress", deploymentId);
        return Response.ok(progress).build();
    }

    /**
     * 标记阶段失败
     * POST /api/deployments/progress/{deploymentId}/fail
     */
    @POST
    @Operation(summary = "标记阶段失败", description = "标记部署任务的某个阶段为失败状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功标记阶段失败"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "部署任务不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @Path("/{deploymentId}/fail")
    @RequiresPermission("deployment:edit")
    public Response markStageFailed(
            @Parameter(description = "部署任务 ID", in = ParameterIn.PATH) @PathParam("deploymentId") UUID deploymentId,
            @Parameter(description = "阶段名称", in = ParameterIn.QUERY) @QueryParam("stage") String stage,
            @Parameter(description = "失败原因", in = ParameterIn.QUERY) @QueryParam("message") String message) {
        if (stage == null || stage.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "stage is required"))
                .build();
        }
        DeploymentProgressDTO progress = progressService.markStageFailed(deploymentId, stage, message);
        recordAuditLog(AuditAction.MARK_STAGE_FAILED, AuditResult.SUCCESS,
                "标记阶段失败：" + stage, "DeploymentProgress", deploymentId);
        return Response.ok(progress).build();
    }

    private void recordAuditLog(AuditAction action, AuditResult result,
                               String description, String resourceType, UUID resourceId) {
        try {
            String username = securityContext != null && securityContext.getUserPrincipal() != null
                    ? securityContext.getUserPrincipal().getName() : "system";
            String clientIp = httpHeaders != null
                    ? httpHeaders.getHeaderString("X-Forwarded-For")
                    : null;
            if (clientIp == null && httpHeaders != null) {
                clientIp = httpHeaders.getHeaderString("X-Real-IP");
            }
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployments/progress");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}