package com.easystation.deployment.resource;

import com.easystation.deployment.dto.DeploymentProgressDTO;
import com.easystation.deployment.dto.DeploymentProgressHistoryDTO;
import com.easystation.deployment.service.DeploymentProgressService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

    /**
     * 获取当前进展
     * GET /api/deployments/progress/{deploymentId}
     */
    @GET
    @Path("/{deploymentId}")
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
        return Response.ok(progress).build();
    }

    /**
     * 标记阶段失败
     * POST /api/deployments/progress/{deploymentId}/fail
     */
    @POST
    @Path("/{deploymentId}/fail")
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
        return Response.ok(progress).build();
    }
}