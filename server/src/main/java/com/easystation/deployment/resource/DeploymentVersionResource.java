package com.easystation.deployment.resource;

import com.easystation.deployment.dto.DeploymentVersionDTO;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.enums.VersionStatus;
import com.easystation.deployment.service.DeploymentVersionService;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.UUID;

/**
 * 部署版本资源
 */
@Path("/api/deployments/versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "部署版本管理", description = "部署版本管理 API")
public class DeploymentVersionResource {

    @Inject
    DeploymentVersionService versionService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;
    
    /**
     * 查询版本列表
     * GET /api/deployments/versions
     */
    @GET
    @Operation(summary = "查询版本列表", description = "分页查询部署版本列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回版本列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<DeploymentVersionDTO> listVersions(
            @Parameter(description = "页码", in = ParameterIn.QUERY) @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量", in = ParameterIn.QUERY) @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @Parameter(description = "应用 ID", in = ParameterIn.QUERY) @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "状态", in = ParameterIn.QUERY) @QueryParam("status") VersionStatus status,
            @Parameter(description = "发布 ID", in = ParameterIn.QUERY) @QueryParam("releaseId") UUID releaseId,
            @Parameter(description = "关键词", in = ParameterIn.QUERY) @QueryParam("keyword") String keyword,
            @Parameter(description = "排序字段", in = ParameterIn.QUERY) @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @Parameter(description = "排序方式", in = ParameterIn.QUERY) @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {

        return versionService.listVersions(pageNum, pageSize, applicationId, status, releaseId, keyword, sortBy, sortOrder);
    }

    /**
     * 获取版本详情
     * GET /api/deployments/versions/{id}
     */
    @GET
    @Operation(summary = "获取版本详情", description = "根据 ID 获取部署版本详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回版本详情"),
        @ApiResponse(responseCode = "404", description = "版本不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public DeploymentVersionDTO getVersion(
            @Parameter(description = "版本 ID", in = ParameterIn.PATH) @PathParam("id") UUID id) {
        return versionService.getVersion(id);
    }

    /**
     * 创建版本
     * POST /api/deployments/versions
     */
    @POST
    @Operation(summary = "创建版本", description = "创建新的部署版本")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "成功创建版本"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public Response createVersion(
            @Parameter(description = "版本 DTO", required = true) @Valid DeploymentVersionDTO dto) {
        String createdBy = securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName() : "system";
        DeploymentVersionDTO created = versionService.createVersion(dto, createdBy);
        recordAuditLog(AuditAction.CREATE_DEPLOYMENT_VERSION, AuditResult.SUCCESS,
                "创建部署版本：" + created.version, "DeploymentVersion", created.id);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * 更新版本
     * PUT /api/deployments/versions/{id}
     */
    @PUT
    @Operation(summary = "更新版本", description = "更新部署版本信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功更新版本"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "版本不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    @Path("/{id}")
    public DeploymentVersionDTO updateVersion(
            @Parameter(description = "版本 ID", in = ParameterIn.PATH) @PathParam("id") UUID id,
            @Parameter(description = "版本 DTO", required = true) @Valid DeploymentVersionDTO dto) {
        DeploymentVersionDTO updated = versionService.updateVersion(id, dto);
        recordAuditLog(AuditAction.UPDATE_DEPLOYMENT_VERSION, AuditResult.SUCCESS,
                "更新部署版本：" + updated.version, "DeploymentVersion", id);
        return updated;
    }

    /**
     * 删除版本
     * DELETE /api/deployments/versions/{id}
     */
    @DELETE
    @Operation(summary = "删除版本", description = "删除部署版本")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "成功删除版本"),
        @ApiResponse(responseCode = "404", description = "版本不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response deleteVersion(
            @Parameter(description = "版本 ID", in = ParameterIn.PATH) @PathParam("id") UUID id) {
        DeploymentVersionDTO dto = versionService.getVersion(id);
        versionService.deleteVersion(id);
        recordAuditLog(AuditAction.DELETE_DEPLOYMENT_VERSION, AuditResult.SUCCESS,
                "删除部署版本：" + (dto != null ? dto.version : id), "DeploymentVersion", id);
        return Response.noContent().build();
    }

    /**
     * 版本比对
     * POST /api/deployments/versions/compare
     */
    @POST
    @Operation(summary = "版本比对", description = "比对两个部署版本的差异")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回比对结果"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/compare")
    public Object compareVersions(
            @Parameter(description = "源版本 ID", in = ParameterIn.QUERY) @QueryParam("fromVersionId") UUID fromVersionId,
            @Parameter(description = "目标版本 ID", in = ParameterIn.QUERY) @QueryParam("toVersionId") UUID toVersionId) {
        return versionService.compareVersions(fromVersionId, toVersionId);
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployments/versions");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}