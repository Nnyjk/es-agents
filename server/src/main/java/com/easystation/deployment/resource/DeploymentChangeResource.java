package com.easystation.deployment.resource;

import com.easystation.deployment.dto.DeploymentChangeDTO;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.enums.ChangeType;
import com.easystation.deployment.service.DeploymentChangeService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 部署变更记录资源
 */
@Path("/api/deployments/changes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class DeploymentChangeResource {

    @Inject
    DeploymentChangeService changeService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;
    
    /**
     * 查询变更记录
     * GET /api/deployments/changes
     */
    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<DeploymentChangeDTO> listChanges(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @QueryParam("versionId") UUID versionId,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("changeType") ChangeType changeType,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {
        
        return changeService.listChanges(pageNum, pageSize, versionId, applicationId, changeType, sortBy, sortOrder);
    }
    
    /**
     * 获取变更详情
     * GET /api/deployments/changes/{id}
     */
    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public DeploymentChangeDTO getChange(@PathParam("id") UUID id) {
        return changeService.getChange(id);
    }
    
    /**
     * 创建变更记录
     * POST /api/deployments/changes
     */
    @POST
    @RequiresPermission("deployment:create")
    public DeploymentChangeDTO createChange(@Valid DeploymentChangeDTO dto) {
        // TODO: 从安全上下文获取当前用户
        String createdBy = securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName() : "system";
        DeploymentChangeDTO created = changeService.createChange(dto, createdBy);
        recordAuditLog(AuditAction.CREATE_DEPLOYMENT_CHANGE, AuditResult.SUCCESS,
                "创建部署变更记录", "DeploymentChange", created.id);
        return created;
    }

    /**
     * 批量创建变更记录
     * POST /api/deployments/changes/batch
     */
    @POST
    @RequiresPermission("deployment:create")
    @Path("/batch")
    public List<DeploymentChangeDTO> batchCreateChanges(@Valid List<DeploymentChangeDTO> dtos) {
        // TODO: 从安全上下文获取当前用户
        String createdBy = securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName() : "system";
        List<DeploymentChangeDTO> created = changeService.batchCreateChanges(dtos, createdBy);
        recordAuditLog(AuditAction.CREATE_DEPLOYMENT_CHANGE, AuditResult.SUCCESS,
                "批量创建部署变更记录，数量：" + created.size(), "DeploymentChange", null);
        return created;
    }

    /**
     * 获取版本的变更记录
     * GET /api/deployments/versions/{versionId}/changes
     */
    @GET
    @RequiresPermission("deployment:view")
    @Path("/versions/{versionId}")
    public List<DeploymentChangeDTO> getVersionChanges(@PathParam("versionId") UUID versionId) {
        return changeService.getVersionChanges(versionId);
    }

    /**
     * 分析变更影响
     * GET /api/deployments/versions/{versionId}/impact
     */
    @GET
    @RequiresPermission("deployment:view")
    @Path("/versions/{versionId}/impact")
    public Map<String, Object> analyzeImpact(@PathParam("versionId") UUID versionId) {
        return changeService.analyzeImpact(versionId);
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployments/changes");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}