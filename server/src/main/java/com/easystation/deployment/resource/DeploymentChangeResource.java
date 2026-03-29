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
@Tag(name = "部署变更管理", description = "部署变更记录管理 API")
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
    @Operation(summary = "查询变更记录", description = "分页查询部署变更记录列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回变更记录列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<DeploymentChangeDTO> listChanges(
            @Parameter(description = "页码", in = ParameterIn.QUERY) @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量", in = ParameterIn.QUERY) @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @Parameter(description = "版本 ID", in = ParameterIn.QUERY) @QueryParam("versionId") UUID versionId,
            @Parameter(description = "应用 ID", in = ParameterIn.QUERY) @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "变更类型", in = ParameterIn.QUERY) @QueryParam("changeType") ChangeType changeType,
            @Parameter(description = "排序字段", in = ParameterIn.QUERY) @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @Parameter(description = "排序方式", in = ParameterIn.QUERY) @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {
        
        return changeService.listChanges(pageNum, pageSize, versionId, applicationId, changeType, sortBy, sortOrder);
    }
    
    /**
     * 获取变更详情
     * GET /api/deployments/changes/{id}
     */
    @GET
    @Operation(summary = "获取变更详情", description = "根据 ID 获取部署变更记录详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回变更记录"),
        @ApiResponse(responseCode = "404", description = "变更记录不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public DeploymentChangeDTO getChange(
            @Parameter(description = "变更记录 ID", in = ParameterIn.PATH) @PathParam("id") UUID id) {
        return changeService.getChange(id);
    }
    
    /**
     * 创建变更记录
     * POST /api/deployments/changes
     */
    @POST
    @Operation(summary = "创建变更记录", description = "创建新的部署变更记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功创建变更记录"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public DeploymentChangeDTO createChange(
            @Parameter(description = "变更记录 DTO", required = true) @Valid DeploymentChangeDTO dto) {
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
    @Operation(summary = "批量创建变更记录", description = "批量创建部署变更记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功批量创建变更记录"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    @Path("/batch")
    public List<DeploymentChangeDTO> batchCreateChanges(
            @Parameter(description = "变更记录 DTO 列表", required = true) @Valid List<DeploymentChangeDTO> dtos) {
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
    @Operation(summary = "获取版本的变更记录", description = "获取指定部署版本的所有变更记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回变更记录列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/versions/{versionId}")
    public List<DeploymentChangeDTO> getVersionChanges(
            @Parameter(description = "版本 ID", in = ParameterIn.PATH) @PathParam("versionId") UUID versionId) {
        return changeService.getVersionChanges(versionId);
    }

    /**
     * 分析变更影响
     * GET /api/deployments/versions/{versionId}/impact
     */
    @GET
    @Operation(summary = "分析变更影响", description = "分析指定部署版本的变更影响范围")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回影响分析结果"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/versions/{versionId}/impact")
    public Map<String, Object> analyzeImpact(
            @Parameter(description = "版本 ID", in = ParameterIn.PATH) @PathParam("versionId") UUID versionId) {
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