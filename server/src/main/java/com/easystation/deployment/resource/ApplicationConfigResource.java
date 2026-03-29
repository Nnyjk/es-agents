package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ApplicationConfig;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.ApplicationConfigDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ApplicationConfigService;
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

import java.util.UUID;

/**
 * 应用配置管理 API
 */
@Path("/api/deployment/applications/{applicationId}/configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "应用配置管理", description = "应用配置查询与管理")
public class ApplicationConfigResource {

    @Inject
    ApplicationConfigService configService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "获取配置列表", description = "分页查询应用配置列表")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<ApplicationConfigDTO> list(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "配置类型") @QueryParam("configType") ApplicationConfig.ConfigType configType,
            @Parameter(description = "页码") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量") @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return configService.listConfigs(pageNum, pageSize, applicationId, environmentId, configType);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取配置详情", description = "根据 ID 查询应用配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回配置详情"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ApplicationConfigDTO get(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "配置 ID") @PathParam("id") UUID id) {
        ApplicationConfigDTO dto = configService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @Operation(summary = "创建配置", description = "创建新的应用配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建配置"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "配置已存在")
    })
    @RequiresPermission("deployment:create")
    public Response create(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            ApplicationConfigDTO dto) {
        dto.applicationId = applicationId;
        ApplicationConfigDTO created = configService.create(dto);
        recordAuditLog(AuditAction.CREATE_APPLICATION_CONFIG, AuditResult.SUCCESS,
                "创建应用配置", "ApplicationConfig", created.id);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新配置", description = "更新应用配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新配置"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public ApplicationConfigDTO update(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "配置 ID") @PathParam("id") UUID id,
            ApplicationConfigDTO dto) {
        ApplicationConfigDTO updated = configService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.UPDATE_APPLICATION_CONFIG, AuditResult.SUCCESS,
                "更新应用配置", "ApplicationConfig", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除配置", description = "删除单个应用配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除配置"),
        @APIResponse(responseCode = "404", description = "配置不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    public Response delete(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "配置 ID") @PathParam("id") UUID id) {
        boolean deleted = configService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DELETE_APPLICATION_CONFIG, AuditResult.SUCCESS,
                "删除应用配置", "ApplicationConfig", id);
        return Response.noContent().build();
    }

    @DELETE
    @Operation(summary = "批量删除配置", description = "删除应用的所有配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功删除配置"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    public Response deleteByApplication(@Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId) {
        long count = configService.deleteByApplication(applicationId);
        recordAuditLog(AuditAction.DELETE_APPLICATION_CONFIG, AuditResult.SUCCESS,
                "删除应用所有配置，数量：" + count, "ApplicationConfig", applicationId);
        return Response.ok().entity("{\"deleted\": " + count + "}").build();
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/applications/configs");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}