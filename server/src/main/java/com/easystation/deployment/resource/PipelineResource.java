package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.PipelineStatus;
import com.easystation.deployment.service.PipelineService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

@Path("/api/deployment/pipelines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PipelineResource {

    @Inject
    PipelineService pipelineService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<PipelineDTO> list(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("name") String name,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("status") PipelineStatus status) {
        return pipelineService.listPipelines(pageNum, pageSize, name, applicationId, status);
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("deployment:view")
    public PipelineDTO get(@PathParam("id") UUID id) {
        return pipelineService.getPipeline(id);
    }

    @POST
    @RequiresPermission("deployment:create")
    public PipelineDTO create(PipelineDTO dto) {
        PipelineDTO created = pipelineService.createPipeline(dto);
        recordAuditLog(AuditAction.CREATE_PIPELINE, AuditResult.SUCCESS,
                "创建流水线：" + created.name, "Pipeline", created.id);
        return created;
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("deployment:edit")
    public PipelineDTO update(@PathParam("id") UUID id, PipelineDTO dto) {
        PipelineDTO updated = pipelineService.updatePipeline(id, dto);
        recordAuditLog(AuditAction.UPDATE_PIPELINE, AuditResult.SUCCESS,
                "更新流水线：" + updated.name, "Pipeline", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("deployment:delete")
    public Response delete(@PathParam("id") UUID id) {
        PipelineDTO dto = pipelineService.getPipeline(id);
        pipelineService.deletePipeline(id);
        recordAuditLog(AuditAction.DELETE_PIPELINE, AuditResult.SUCCESS,
                "删除流水线：" + (dto != null ? dto.name : id), "Pipeline", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/trigger")
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO trigger(@PathParam("id") UUID id,
                                        @QueryParam("triggeredBy") @DefaultValue("system") String triggeredBy) {
        String username = securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName() : triggeredBy;
        PipelineExecutionDTO execution = pipelineService.triggerPipeline(id, username);
        recordAuditLog(AuditAction.TRIGGER_PIPELINE, AuditResult.SUCCESS,
                "触发流水线执行", "PipelineExecution", execution.id);
        return execution;
    }

    @GET
    @Path("/{id}/executions")
    @RequiresPermission("deployment:view")
    public List<PipelineExecutionDTO> getExecutions(@PathParam("id") UUID id) {
        return pipelineService.getPipelineExecutions(id);
    }

    @GET
    @Path("/executions/{executionId}")
    @RequiresPermission("deployment:view")
    public PipelineExecutionDTO getExecutionDetail(@PathParam("executionId") UUID executionId) {
        return pipelineService.getExecutionDetail(executionId);
    }

    @POST
    @Path("/executions/{executionId}/cancel")
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO cancelExecution(@PathParam("executionId") UUID executionId) {
        PipelineExecutionDTO execution = pipelineService.cancelPipelineExecution(executionId);
        recordAuditLog(AuditAction.CANCEL_PIPELINE, AuditResult.SUCCESS,
                "取消流水线执行", "PipelineExecution", executionId);
        return execution;
    }

    @POST
    @Path("/executions/{executionId}/retry")
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO retryExecution(@PathParam("executionId") UUID executionId) {
        PipelineExecutionDTO execution = pipelineService.retryPipelineExecution(executionId);
        recordAuditLog(AuditAction.RETRY_PIPELINE, AuditResult.SUCCESS,
                "重试流水线执行", "PipelineExecution", executionId);
        return execution;
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/pipelines");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}