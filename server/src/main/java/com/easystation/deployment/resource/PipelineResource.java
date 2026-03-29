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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/deployment/pipelines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "部署流水线管理", description = "部署流水线配置与执行管理")
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
    @Operation(summary = "获取流水线列表", description = "分页查询部署流水线列表，支持名称和状态筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回流水线列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<PipelineDTO> list(
            @Parameter(description = "页码") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量") @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Parameter(description = "流水线名称") @QueryParam("name") String name,
            @Parameter(description = "应用 ID") @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "流水线状态") @QueryParam("status") PipelineStatus status) {
        return pipelineService.listPipelines(pageNum, pageSize, name, applicationId, status);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取流水线详情", description = "根据 ID 查询单个部署流水线详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回流水线详情"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PipelineDTO get(@Parameter(description = "流水线 ID") @PathParam("id") UUID id) {
        return pipelineService.getPipeline(id);
    }

    @POST
    @Operation(summary = "创建流水线", description = "创建新的部署流水线")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功创建流水线"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "流水线名称已存在")
    })
    @RequiresPermission("deployment:create")
    public PipelineDTO create(PipelineDTO dto) {
        PipelineDTO created = pipelineService.createPipeline(dto);
        recordAuditLog(AuditAction.CREATE_PIPELINE, AuditResult.SUCCESS,
                "创建流水线：" + created.name, "Pipeline", created.id);
        return created;
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新流水线", description = "更新指定部署流水线的配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新流水线"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "流水线名称已存在")
    })
    @RequiresPermission("deployment:edit")
    public PipelineDTO update(@Parameter(description = "流水线 ID") @PathParam("id") UUID id, PipelineDTO dto) {
        PipelineDTO updated = pipelineService.updatePipeline(id, dto);
        recordAuditLog(AuditAction.UPDATE_PIPELINE, AuditResult.SUCCESS,
                "更新流水线：" + updated.name, "Pipeline", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除流水线", description = "删除指定的部署流水线")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除流水线"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "流水线仍有关联资源，无法删除")
    })
    @RequiresPermission("deployment:delete")
    public Response delete(@Parameter(description = "流水线 ID") @PathParam("id") UUID id) {
        PipelineDTO dto = pipelineService.getPipeline(id);
        pipelineService.deletePipeline(id);
        recordAuditLog(AuditAction.DELETE_PIPELINE, AuditResult.SUCCESS,
                "删除流水线：" + (dto != null ? dto.name : id), "Pipeline", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/trigger")
    @Operation(summary = "触发流水线执行", description = "手动触发指定流水线的执行")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功触发流水线执行"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO trigger(@Parameter(description = "流水线 ID") @PathParam("id") UUID id,
                                        @Parameter(description = "触发人") @QueryParam("triggeredBy") @DefaultValue("system") String triggeredBy) {
        String username = securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName() : triggeredBy;
        PipelineExecutionDTO execution = pipelineService.triggerPipeline(id, username);
        recordAuditLog(AuditAction.TRIGGER_PIPELINE, AuditResult.SUCCESS,
                "触发流水线执行", "PipelineExecution", execution.id);
        return execution;
    }

    @GET
    @Path("/{id}/executions")
    @Operation(summary = "获取流水线执行历史", description = "查询指定流水线的所有执行记录")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回执行历史列表"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public List<PipelineExecutionDTO> getExecutions(@Parameter(description = "流水线 ID") @PathParam("id") UUID id) {
        return pipelineService.getPipelineExecutions(id);
    }

    @GET
    @Path("/executions/{executionId}")
    @Operation(summary = "获取执行详情", description = "查询单次流水线执行的详细信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回执行详情"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PipelineExecutionDTO getExecutionDetail(@Parameter(description = "执行 ID") @PathParam("executionId") UUID executionId) {
        return pipelineService.getExecutionDetail(executionId);
    }

    @POST
    @Path("/executions/{executionId}/cancel")
    @Operation(summary = "取消执行", description = "取消正在执行的流水线")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功取消执行"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO cancelExecution(@Parameter(description = "执行 ID") @PathParam("executionId") UUID executionId) {
        PipelineExecutionDTO execution = pipelineService.cancelPipelineExecution(executionId);
        recordAuditLog(AuditAction.CANCEL_PIPELINE, AuditResult.SUCCESS,
                "取消流水线执行", "PipelineExecution", executionId);
        return execution;
    }

    @POST
    @Path("/executions/{executionId}/retry")
    @Operation(summary = "重试执行", description = "重新执行失败或取消的流水线")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功重试执行"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO retryExecution(@Parameter(description = "执行 ID") @PathParam("executionId") UUID executionId) {
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