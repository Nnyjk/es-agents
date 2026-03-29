package com.easystation.deployment.resource;

import com.easystation.deployment.dto.DeploymentStatisticsDTO;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.DeploymentHistoryService;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 部署历史记录资源
 */
@Path("/api/deployments/history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class DeploymentHistoryResource {

    @Inject
    DeploymentHistoryService historyService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;
    
    /**
     * 查询部署历史
     * GET /api/deployments/history
     */
    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<Map<String, Object>> listHistory(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("version") String version,
            @QueryParam("status") String status,
            @QueryParam("triggeredBy") String triggeredBy,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("keyword") String keyword,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {
        
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        
        return historyService.listHistory(
                pageNum, pageSize, applicationId, environmentId,
                version, status, triggeredBy, startTime, endTime,
                keyword, sortBy, sortOrder
        );
    }
    
    /**
     * 获取部署详情
     * GET /api/deployments/history/{releaseId}
     */
    @GET
    @Path("/{releaseId}")
    @RequiresPermission("deployment:view")
    public Map<String, Object> getHistoryDetail(@PathParam("releaseId") UUID releaseId) {
        return historyService.getHistoryDetail(releaseId);
    }
    
    /**
     * 获取部署统计
     * GET /api/deployments/history/statistics
     */
    @GET
    @Path("/statistics")
    @RequiresPermission("deployment:view")
    public DeploymentStatisticsDTO getStatistics(
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) {
        
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        
        return historyService.getStatistics(applicationId, environmentId, startTime, endTime);
    }
    
    /**
     * 导出部署历史
     * GET /api/deployments/history/export
     */
    @GET
    @Path("/export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RequiresPermission("deployment:view")
    public Response exportHistory(
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("fields") String fieldsStr) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        List<String> fields = fieldsStr != null ?
                List.of(fieldsStr.split(",")) : List.of();

        List<Map<String, Object>> data = historyService.exportHistory(
                applicationId, environmentId, startTime, endTime, fields
        );

        // 简单的 CSV 导出
        StringBuilder csv = new StringBuilder();
        if (!data.isEmpty()) {
            // Header
            csv.append(String.join(",", data.get(0).keySet())).append("\n");
            // Data
            for (Map<String, Object> row : data) {
                csv.append(row.values().stream()
                        .map(v -> v == null ? "" : v.toString())
                        .collect(java.util.stream.Collectors.joining(",")))
                        .append("\n");
            }
        }

        recordAuditLog(AuditAction.EXPORT_DATA, AuditResult.SUCCESS,
                "导出部署历史，数量：" + data.size(), "DeploymentHistory", null);

        return Response.ok(csv.toString())
                .header("Content-Disposition", "attachment; filename=deployment_history.csv")
                .build();
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployments/history");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}