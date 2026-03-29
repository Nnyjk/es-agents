package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentLogRecord;
import com.easystation.agent.dto.AgentLogRecord.DeploymentLogQuery;
import com.easystation.agent.dto.AgentLogRecord.CommandLogQuery;
import com.easystation.agent.dto.AgentLogRecord.TaskLogRecord;
import com.easystation.agent.service.AgentLogService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Path("/agents/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 日志管理", description = "Agent 日志查询、统计 API")
public class AgentLogResource {

    @Inject
    AgentLogService agentLogService;

    @GET
    @Path("/{agentId}")
    @Operation(summary = "查询 Agent 日志", description = "支持分页、级别筛选、关键词搜索和时间范围筛选")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回日志列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @Parameter(name = "limit", description = "返回数量限制（默认 100）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @Parameter(name = "level", description = "日志级别筛选（INFO/WARN/ERROR）", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式）", required = false)
    @RequiresPermission("agent:view")
    public Response queryLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("level") String level,
            @QueryParam("keyword") String keyword,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        var query = new AgentLogRecord.Query(
            agentId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            level,
            keyword,
            null, // TODO: parse startTime
            null  // TODO: parse endTime
        );
        
        var result = agentLogService.queryLogs(query);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{agentId}/stats")
    @Operation(summary = "获取 Agent 日志统计信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回日志统计"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getLogStats(@PathParam("agentId") UUID agentId) {
        var stats = agentLogService.getLogStats(agentId);
        return Response.ok(stats).build();
    }

    @GET
    @Path("/{agentId}/tail")
    @Operation(summary = "获取 Agent 最新日志（Tail 模式）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回最新日志"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @Parameter(name = "lines", description = "返回行数（默认 50）", required = false)
    @RequiresPermission("agent:view")
    public Response tailLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("lines") Integer lines) {
        
        var logs = agentLogService.getLatestLogs(agentId, lines != null ? lines : 50);
        return Response.ok(logs).build();
    }

    @GET
    @Path("/{agentId}/deployment")
    @Operation(summary = "获取部署过程日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回部署日志"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @Parameter(name = "deploymentId", description = "部署记录 ID（可选筛选）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（默认 100）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式）", required = false)
    @RequiresPermission("agent:view")
    public Response getDeploymentLogs(
            @PathParam("agentId") UUID agentId,
            @QueryParam("deploymentId") UUID deploymentId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        LocalDateTime start = startTime != null ? parseDateTime(startTime) : null;
        LocalDateTime end = endTime != null ? parseDateTime(endTime) : null;
        
        var query = new AgentLogRecord.DeploymentLogQuery(
            agentId,
            deploymentId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            start,
            end
        );
        
        var result = agentLogService.queryDeploymentLogs(query);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{agentId}/command/{executionId}")
    @Operation(summary = "获取命令执行日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回命令执行日志"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @Parameter(name = "executionId", description = "命令执行记录 ID", required = true)
    @Parameter(name = "limit", description = "返回数量限制（默认 100）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式）", required = false)
    @RequiresPermission("agent:view")
    public Response getCommandLogs(
            @PathParam("agentId") UUID agentId,
            @PathParam("executionId") UUID executionId,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        
        LocalDateTime start = startTime != null ? parseDateTime(startTime) : null;
        LocalDateTime end = endTime != null ? parseDateTime(endTime) : null;
        
        var query = new AgentLogRecord.CommandLogQuery(
            agentId,
            executionId,
            limit != null ? limit : 100,
            offset != null ? offset : 0,
            start,
            end
        );
        
        var result = agentLogService.queryCommandLogs(query);
        return Response.ok(result).build();
    }

    @GET
    @Path("/tasks/{taskId}")
    @Operation(summary = "获取特定任务的执行日志")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务日志"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "taskId", description = "任务 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getTaskLog(@PathParam("taskId") UUID taskId) {
        var result = agentLogService.getTaskLog(taskId);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Task not found: " + taskId)
                .build();
        }
        return Response.ok(result).build();
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            // 尝试其他格式
            try {
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
