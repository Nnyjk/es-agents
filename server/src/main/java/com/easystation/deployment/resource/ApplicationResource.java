package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.ApplicationStatus;
import com.easystation.deployment.service.ApplicationService;
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

import java.util.UUID;

@Path("/api/deployment/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "应用管理", description = "部署应用管理 API")
public class ApplicationResource {

    @Inject
    ApplicationService applicationService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "获取应用列表", description = "分页查询应用列表，支持按名称、项目、负责人、状态筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回应用列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<ApplicationDTO> list(
            @Parameter(description = "页码", example = "1") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量", example = "10") @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Parameter(description = "应用名称（模糊匹配）") @QueryParam("name") String name,
            @Parameter(description = "项目名称") @QueryParam("project") String project,
            @Parameter(description = "负责人") @QueryParam("owner") String owner,
            @Parameter(description = "应用状态") @QueryParam("status") ApplicationStatus status) {
        return applicationService.listApplications(pageNum, pageSize, name, project, owner, status);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取应用详情", description = "根据 ID 查询单个应用详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回应用详情"),
        @APIResponse(responseCode = "404", description = "应用不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ApplicationDTO get(@Parameter(description = "应用 ID") @PathParam("id") UUID id) {
        return applicationService.getApplication(id);
    }

    @POST
    @Operation(summary = "创建应用", description = "创建新的部署应用")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功创建应用"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "应用名称已存在")
    })
    @RequiresPermission("deployment:create")
    public ApplicationDTO create(ApplicationDTO dto) {
        ApplicationDTO created = applicationService.createApplication(dto);
        recordAuditLog(AuditAction.CREATE_APPLICATION, AuditResult.SUCCESS,
                "创建应用：" + created.name, "Application", created.id);
        return created;
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新应用", description = "更新指定应用的配置信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新应用"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "应用不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "应用名称已存在")
    })
    @RequiresPermission("deployment:edit")
    public ApplicationDTO update(@Parameter(description = "应用 ID") @PathParam("id") UUID id, ApplicationDTO dto) {
        ApplicationDTO updated = applicationService.updateApplication(id, dto);
        recordAuditLog(AuditAction.UPDATE_APPLICATION, AuditResult.SUCCESS,
                "更新应用：" + updated.name, "Application", id);
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除应用", description = "删除指定的部署应用")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除应用"),
        @APIResponse(responseCode = "404", description = "应用不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "应用仍有关联资源，无法删除")
    })
    @RequiresPermission("deployment:delete")
    public Response delete(@Parameter(description = "应用 ID") @PathParam("id") UUID id) {
        ApplicationDTO dto = applicationService.getApplication(id);
        applicationService.deleteApplication(id);
        recordAuditLog(AuditAction.DELETE_APPLICATION, AuditResult.SUCCESS,
                "删除应用：" + (dto != null ? dto.name : id), "Application", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/archive")
    @Operation(summary = "归档应用", description = "归档指定的部署应用")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功归档应用"),
        @APIResponse(responseCode = "404", description = "应用不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public ApplicationDTO archive(@Parameter(description = "应用 ID") @PathParam("id") UUID id) {
        ApplicationDTO archived = applicationService.archiveApplication(id);
        recordAuditLog(AuditAction.ARCHIVE_APPLICATION, AuditResult.SUCCESS,
                "归档应用：" + archived.name, "Application", id);
        return archived;
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/applications");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}