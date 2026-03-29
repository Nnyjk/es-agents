package com.easystation.deployment.resource;

import com.easystation.deployment.domain.DeployStrategy;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.DeployStrategyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.DeployStrategyService;
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

/**
 * 部署策略管理 API
 */
@Path("/api/deployment/applications/{applicationId}/strategies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "部署策略管理", description = "应用部署策略配置与管理")
public class DeployStrategyResource {

    @Inject
    DeployStrategyService strategyService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "获取策略列表", description = "分页查询部署策略列表，支持环境和类型筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回策略列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<DeployStrategyDTO> list(
            @Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "策略类型") @QueryParam("type") DeployStrategy.StrategyType type,
            @Parameter(description = "页码") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量") @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return strategyService.listStrategies(pageNum, pageSize, applicationId, environmentId, type);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/all")
    public List<DeployStrategyDTO> listAll(@PathParam("applicationId") UUID applicationId) {
        return strategyService.getByApplicationId(applicationId);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/default")
    public DeployStrategyDTO getDefault(
            @PathParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId) {
        DeployStrategyDTO dto = strategyService.getDefaultStrategy(applicationId, environmentId);
        if (dto == null) {
            throw new WebApplicationException("No default strategy found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public DeployStrategyDTO get(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    public Response create(
            @PathParam("applicationId") UUID applicationId,
            DeployStrategyDTO dto) {
        dto.applicationId = applicationId;
        DeployStrategyDTO created = strategyService.create(dto);
        recordAuditLog(AuditAction.CREATE_DEPLOY_STRATEGY, AuditResult.SUCCESS,
                "创建部署策略", "DeployStrategy", created.id);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @RequiresPermission("deployment:edit")
    @Path("/{id}")
    public DeployStrategyDTO update(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id,
            DeployStrategyDTO dto) {
        DeployStrategyDTO updated = strategyService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.UPDATE_DEPLOY_STRATEGY, AuditResult.SUCCESS,
                "更新部署策略", "DeployStrategy", id);
        return updated;
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response delete(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        boolean deleted = strategyService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DELETE_DEPLOY_STRATEGY, AuditResult.SUCCESS,
                "删除部署策略", "DeployStrategy", id);
        return Response.noContent().build();
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    public Response deleteByApplication(@PathParam("applicationId") UUID applicationId) {
        long count = strategyService.deleteByApplication(applicationId);
        recordAuditLog(AuditAction.DELETE_DEPLOY_STRATEGY, AuditResult.SUCCESS,
                "删除应用所有部署策略，数量：" + count, "DeployStrategy", applicationId);
        return Response.ok().entity("{\"deleted\": " + count + "}").build();
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/activate")
    public DeployStrategyDTO activate(@PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.setActive(id, true);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.ACTIVATE_RESOURCE, AuditResult.SUCCESS,
                "激活部署策略", "DeployStrategy", id);
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/deactivate")
    public DeployStrategyDTO deactivate(@PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.setActive(id, false);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DEACTIVATE_RESOURCE, AuditResult.SUCCESS,
                "停用部署策略", "DeployStrategy", id);
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/default")
    public DeployStrategyDTO setDefault(@PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.setDefault(id);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.SET_DEFAULT_RESOURCE, AuditResult.SUCCESS,
                "设置默认部署策略", "DeployStrategy", id);
        return dto;
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/applications/strategies");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}