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
    @Path("/{deploymentId}")
    @RequiresPermission("deployment:view")
    public Response getCurrentProgress(@PathParam("deploymentId") UUID deploymentId) {
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
    @Path("/{deploymentId}/history")
    @RequiresPermission("deployment:view")
    public Response getProgressHistory(@PathParam("deploymentId") UUID deploymentId) {
        List<DeploymentProgressDTO> history = progressService.getProgressHistory(deploymentId);
        return Response.ok(history).build();
    }

    /**
     * 获取状态变更历史
     * GET /api/deployments/progress/{deploymentId}/status-history
     */
    @GET
    @Path("/{deploymentId}/status-history")
    @RequiresPermission("deployment:view")
    public Response getStatusHistory(@PathParam("deploymentId") UUID deploymentId) {
        List<DeploymentProgressHistoryDTO> statusHistory = progressService.getStatusHistory(deploymentId);
        return Response.ok(statusHistory).build();
    }

    /**
     * 获取总体进度百分比
     * GET /api/deployments/progress/{deploymentId}/overall
     */
    @GET
    @Path("/{deploymentId}/overall")
    @RequiresPermission("deployment:view")
    public Response getOverallProgress(@PathParam("deploymentId") UUID deploymentId) {
        int overallProgress = progressService.calculateOverallProgress(deploymentId);
        return Response.ok(Map.of("deploymentId", deploymentId, "overallProgress", overallProgress)).build();
    }

    /**
     * 标记阶段完成
     * POST /api/deployments/progress/{deploymentId}/complete
     */
    @POST
    @Path("/{deploymentId}/complete")
    @RequiresPermission("deployment:edit")
    public Response markStageComplete(
            @PathParam("deploymentId") UUID deploymentId,
            @QueryParam("stage") String stage,
            @QueryParam("message") String message) {
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
    @Path("/{deploymentId}/fail")
    @RequiresPermission("deployment:edit")
    public Response markStageFailed(
            @PathParam("deploymentId") UUID deploymentId,
            @QueryParam("stage") String stage,
            @QueryParam("message") String message) {
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