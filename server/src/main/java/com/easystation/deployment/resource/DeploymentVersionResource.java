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
    @RequiresPermission("deployment:view")
    public PageResultDTO<DeploymentVersionDTO> listVersions(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("status") VersionStatus status,
            @QueryParam("releaseId") UUID releaseId,
            @QueryParam("keyword") String keyword,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {

        return versionService.listVersions(pageNum, pageSize, applicationId, status, releaseId, keyword, sortBy, sortOrder);
    }

    /**
     * 获取版本详情
     * GET /api/deployments/versions/{id}
     */
    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public DeploymentVersionDTO getVersion(@PathParam("id") UUID id) {
        return versionService.getVersion(id);
    }

    /**
     * 创建版本
     * POST /api/deployments/versions
     */
    @POST
    @RequiresPermission("deployment:create")
    public Response createVersion(@Valid DeploymentVersionDTO dto) {
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
    @RequiresPermission("deployment:edit")
    @Path("/{id}")
    public DeploymentVersionDTO updateVersion(@PathParam("id") UUID id, @Valid DeploymentVersionDTO dto) {
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
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response deleteVersion(@PathParam("id") UUID id) {
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
    @RequiresPermission("deployment:view")
    @Path("/compare")
    public Object compareVersions(
            @QueryParam("fromVersionId") UUID fromVersionId,
            @QueryParam("toVersionId") UUID toVersionId) {
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