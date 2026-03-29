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
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
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
    @Path("/all")
    @Operation(summary = "获取所有策略", description = "获取应用的所有部署策略")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回策略列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public List<DeployStrategyDTO> listAll(@Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId) {
        return strategyService.getByApplicationId(applicationId);
    }

    @GET
    @Path("/default")
    @Operation(summary = "获取默认策略", description = "获取应用的默认部署策略")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回默认策略"),
        @APIResponse(responseCode = "404", description = "默认策略不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public DeployStrategyDTO getDefault(
            @Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId) {
        DeployStrategyDTO dto = strategyService.getDefaultStrategy(applicationId, environmentId);
        if (dto == null) {
            throw new WebApplicationException("No default strategy found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取策略详情", description = "根据 ID 查询部署策略详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回策略详情"),
        @APIResponse(responseCode = "404", description = "策略不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public DeployStrategyDTO get(
            @Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "策略 ID") @PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @Operation(summary = "创建策略", description = "创建新的部署策略配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建策略"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "策略已存在")
    })
    @RequiresPermission("deployment:create")
    public Response create(
            @Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId,
            DeployStrategyDTO dto) {
        dto.applicationId = applicationId;
        DeployStrategyDTO created = strategyService.create(dto);
        recordAuditLog(AuditAction.CREATE_DEPLOY_STRATEGY, AuditResult.SUCCESS,
                "创建部署策略", "DeployStrategy", created.id);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新策略", description = "更新部署策略配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新策略"),
        @APIResponse(responseCode = "404", description = "策略不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public DeployStrategyDTO update(
            @Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "策略 ID") @PathParam("id") UUID id,
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
    @Path("/{id}")
    @Operation(summary = "删除策略", description = "删除部署策略配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除策略"),
        @APIResponse(responseCode = "404", description = "策略不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    public Response delete(
            @Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "策略 ID") @PathParam("id") UUID id) {
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
    @Path("/{id}/default")
    @Operation(summary = "设为默认策略", description = "设置默认部署策略")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功设置默认策略"),
        @APIResponse(responseCode = "404", description = "策略不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public DeployStrategyDTO setDefault(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "策略 ID") @PathParam("id") UUID id) {
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