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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "部署历史管理", description = "部署历史记录管理 API")
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
    @Operation(summary = "查询部署历史", description = "分页查询部署历史记录列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回部署历史列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<Map<String, Object>> listHistory(
            @Parameter(description = "页码", in = ParameterIn.QUERY) @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量", in = ParameterIn.QUERY) @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @Parameter(description = "应用 ID", in = ParameterIn.QUERY) @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID", in = ParameterIn.QUERY) @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "版本号", in = ParameterIn.QUERY) @QueryParam("version") String version,
            @Parameter(description = "状态", in = ParameterIn.QUERY) @QueryParam("status") String status,
            @Parameter(description = "触发人", in = ParameterIn.QUERY) @QueryParam("triggeredBy") String triggeredBy,
            @Parameter(description = "开始时间", in = ParameterIn.QUERY) @QueryParam("startTime") String startTimeStr,
            @Parameter(description = "结束时间", in = ParameterIn.QUERY) @QueryParam("endTime") String endTimeStr,
            @Parameter(description = "关键词", in = ParameterIn.QUERY) @QueryParam("keyword") String keyword,
            @Parameter(description = "排序字段", in = ParameterIn.QUERY) @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @Parameter(description = "排序方式", in = ParameterIn.QUERY) @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {
        
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
    @Operation(summary = "获取部署详情", description = "根据发布 ID 获取部署历史详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回部署详情"),
        @ApiResponse(responseCode = "404", description = "部署记录不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/{releaseId}")
    public Map<String, Object> getHistoryDetail(
            @Parameter(description = "发布 ID", in = ParameterIn.PATH) @PathParam("releaseId") UUID releaseId) {
        return historyService.getHistoryDetail(releaseId);
    }
    
    /**
     * 获取部署统计
     * GET /api/deployments/history/statistics
     */
    @GET
    @Operation(summary = "获取部署统计", description = "获取部署历史统计数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回统计数据"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/statistics")
    public DeploymentStatisticsDTO getStatistics(
            @Parameter(description = "应用 ID", in = ParameterIn.QUERY) @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID", in = ParameterIn.QUERY) @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "开始时间", in = ParameterIn.QUERY) @QueryParam("startTime") String startTimeStr,
            @Parameter(description = "结束时间", in = ParameterIn.QUERY) @QueryParam("endTime") String endTimeStr) {
        
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        
        return historyService.getStatistics(applicationId, environmentId, startTime, endTime);
    }
    
    /**
     * 导出部署历史
     * GET /api/deployments/history/export
     */
    @GET
    @Operation(summary = "导出部署历史", description = "导出部署历史记录为 CSV 文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回 CSV 文件"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    @Path("/export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportHistory(
            @Parameter(description = "应用 ID", in = ParameterIn.QUERY) @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID", in = ParameterIn.QUERY) @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "开始时间", in = ParameterIn.QUERY) @QueryParam("startTime") String startTimeStr,
            @Parameter(description = "结束时间", in = ParameterIn.QUERY) @QueryParam("endTime") String endTimeStr,
            @Parameter(description = "导出字段", in = ParameterIn.QUERY) @QueryParam("fields") String fieldsStr) {

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