package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.EnvironmentType;
import com.easystation.deployment.service.EnvironmentService;
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

@Path("/api/deployment/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "环境管理", description = "部署环境管理 API")
public class EnvironmentResource {

    @Inject
    EnvironmentService environmentService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "获取环境列表", description = "分页查询部署环境列表，支持按名称、类型、状态筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回环境列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("environment:view")
    public PageResultDTO<EnvironmentDTO> list(
            @Parameter(description = "页码", example = "1") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量", example = "10") @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Parameter(description = "环境名称（模糊匹配）") @QueryParam("name") String name,
            @Parameter(description = "环境类型") @QueryParam("environmentType") EnvironmentType environmentType,
            @Parameter(description = "是否激活") @QueryParam("active") Boolean active) {
        return environmentService.listEnvironments(pageNum, pageSize, name, environmentType, active);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取环境详情", description = "根据 ID 查询单个环境详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回环境详情"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("environment:view")
    public EnvironmentDTO get(@Parameter(description = "环境 ID") @PathParam("id") UUID id) {
        return environmentService.getEnvironment(id);
    }

    @POST
    @Operation(summary = "创建环境", description = "创建新的部署环境")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功创建环境"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "环境名称已存在")
    })
    @RequiresPermission("environment:create")
    public EnvironmentDTO create(EnvironmentDTO dto) {
        EnvironmentDTO created = environmentService.createEnvironment(dto);
        recordAuditLog(AuditAction.CREATE_ENVIRONMENT, AuditResult.SUCCESS,
                "创建环境：" + created.name, "Environment", created.id);
        return created;
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新环境", description = "更新指定环境的配置信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新环境"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "环境名称已存在")
    })
    @RequiresPermission("environment:edit")
    public EnvironmentDTO update(@Parameter(description = "环境 ID") @PathParam("id") UUID id, EnvironmentDTO dto) {
        EnvironmentDTO updated = environmentService.updateEnvironment(id, dto);
        recordAuditLog(AuditAction.UPDATE_ENVIRONMENT, AuditResult.SUCCESS,
                "更新环境：" + updated.name, "Environment", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除环境", description = "删除指定的部署环境")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除环境"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "环境仍有关联资源，无法删除")
    })
    @RequiresPermission("environment:delete")
    public Response delete(@Parameter(description = "环境 ID") @PathParam("id") UUID id) {
        EnvironmentDTO dto = environmentService.getEnvironment(id);
        environmentService.deleteEnvironment(id);
        recordAuditLog(AuditAction.DELETE_ENVIRONMENT, AuditResult.SUCCESS,
                "删除环境：" + (dto != null ? dto.name : id), "Environment", id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/resources")
    @Operation(summary = "获取环境资源列表", description = "查询指定环境下的所有资源")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回资源列表"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("environment:view")
    public List<EnvironmentResourceDTO> getResources(@Parameter(description = "环境 ID") @PathParam("id") UUID id) {
        return environmentService.getEnvironmentResources(id);
    }

    @GET
    @Path("/{id}/applications")
    @Operation(summary = "获取环境应用列表", description = "查询指定环境下部署的所有应用")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回应用列表"),
        @APIResponse(responseCode = "404", description = "环境不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("environment:view")
    public List<EnvironmentApplicationDTO> getApplications(@Parameter(description = "环境 ID") @PathParam("id") UUID id) {
        return environmentService.getEnvironmentApplications(id);
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/environments");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}