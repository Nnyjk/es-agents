package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ApplicationDependency;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.ApplicationDependencyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ApplicationDependencyService;
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
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * 应用依赖管理 API
 */
@Path("/api/deployment/applications/{applicationId}/dependencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "应用依赖管理", description = "应用依赖配置与管理")
public class ApplicationDependencyResource {

    @Inject
    ApplicationDependencyService dependencyService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "获取依赖列表", description = "分页查询应用依赖列表")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回依赖列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<ApplicationDependencyDTO> list(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "依赖类型") @QueryParam("type") ApplicationDependency.DependencyType type,
            @Parameter(description = "页码") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量") @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return dependencyService.listDependencies(pageNum, pageSize, applicationId, environmentId, type);
    }

    @GET
    @Path("/all")
    @Operation(summary = "获取全部依赖", description = "查询应用的所有依赖列表")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回依赖列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public List<ApplicationDependencyDTO> listAll(@Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId) {
        return dependencyService.getByApplicationId(applicationId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取依赖详情", description = "根据 ID 查询应用依赖")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回依赖详情"),
        @APIResponse(responseCode = "404", description = "依赖不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ApplicationDependencyDTO get(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "依赖 ID") @PathParam("id") UUID id) {
        ApplicationDependencyDTO dto = dependencyService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @Operation(summary = "创建依赖", description = "创建新的应用依赖配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建依赖"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "依赖已存在")
    })
    @RequiresPermission("deployment:create")
    public Response create(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            ApplicationDependencyDTO dto) {
        dto.applicationId = applicationId;
        ApplicationDependencyDTO created = dependencyService.create(dto);
        recordAuditLog(AuditAction.CREATE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "创建应用依赖", "ApplicationDependency", created.id);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新依赖", description = "更新应用依赖配置")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新依赖"),
        @APIResponse(responseCode = "404", description = "依赖不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public ApplicationDependencyDTO update(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "依赖 ID") @PathParam("id") UUID id,
            ApplicationDependencyDTO dto) {
        ApplicationDependencyDTO updated = dependencyService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.UPDATE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "更新应用依赖", "ApplicationDependency", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除依赖", description = "删除单个应用依赖")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除依赖"),
        @APIResponse(responseCode = "404", description = "依赖不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    public Response delete(
            @Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId,
            @Parameter(description = "依赖 ID") @PathParam("id") UUID id) {
        boolean deleted = dependencyService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Dependency not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DELETE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "删除应用依赖", "ApplicationDependency", id);
        return Response.noContent().build();
    }

    @DELETE
    @Operation(summary = "批量删除依赖", description = "删除应用的所有依赖")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功删除依赖"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    public Response deleteByApplication(@Parameter(description = "应用 ID", in = ParameterIn.PATH) @PathParam("applicationId") UUID applicationId) {
        long count = dependencyService.deleteByApplication(applicationId);
        recordAuditLog(AuditAction.DELETE_APPLICATION_DEPENDENCY, AuditResult.SUCCESS,
                "删除应用所有依赖，数量：" + count, "ApplicationDependency", applicationId);
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/applications/dependencies");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}