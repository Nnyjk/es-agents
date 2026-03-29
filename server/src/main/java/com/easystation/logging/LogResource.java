package com.easystation.logging;

import com.easystation.logging.dto.LogFileInfo;
import com.easystation.logging.dto.LogStats;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志管理 REST API
 */
@Path("/api/v1/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "logs", description = "日志管理 API")
public class LogResource {
    
    @Inject
    LogManagementService logManagementService;
    
    /**
     * 获取日志文件列表
     */
    @GET
    @Operation(summary = "获取日志文件列表", description = "返回所有日志文件（包括归档文件）的详细信息")
    public Response getLogFiles() {
        List<LogFileInfo> logFiles = logManagementService.getLogFiles();
        return Response.ok(logFiles).build();
    }
    
    /**
     * 获取日志统计信息
     */
    @GET
    @Path("/stats")
    @Operation(summary = "获取日志统计信息", description = "返回日志文件的统计信息，包括总大小、文件数量等")
    public Response getLogStats() {
        LogStats stats = logManagementService.getLogStats();
        return Response.ok(stats).build();
    }
    
    /**
     * 手动触发日志轮转
     */
    @POST
    @Path("/rotate")
    @Operation(summary = "手动触发日志轮转", description = "请求立即执行日志轮转（实际轮转由 Logback 配置自动处理）")
    public Response rotateLogs() {
        logManagementService.triggerRotation();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "日志轮转请求已接收，将由 Logback 根据配置自动执行");
        response.put("status", "success");
        
        return Response.ok(response).build();
    }
    
    /**
     * 清理过期归档日志
     */
    @DELETE
    @Path("/archive")
    @Operation(summary = "清理过期归档日志", description = "删除 30 天前的归档日志文件")
    public Response cleanupArchives(@DefaultValue("30") @QueryParam("days") int retentionDays) {
        int deletedCount = logManagementService.cleanupOldArchives(retentionDays);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "归档清理完成");
        response.put("deletedCount", deletedCount);
        response.put("retentionDays", retentionDays);
        response.put("status", "success");
        
        return Response.ok(response).build();
    }
}
