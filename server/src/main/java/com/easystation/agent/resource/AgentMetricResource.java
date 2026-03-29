package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentMetricRecord;
import com.easystation.agent.service.AgentMetricService;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/agents/monitoring")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 监控管理", description = "Agent 性能指标上报、查询、告警 API")
public class AgentMetricResource {

    @Inject
    AgentMetricService agentMetricService;

    @POST
    @Path("/metrics")
    @Operation(summary = "上报性能指标")
    @APIResponses({
        @APIResponse(responseCode = "202", description = "指标上报成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "report", description = "性能指标上报数据", required = true)
    @RequiresPermission("agent:execute")
    public Response reportMetric(AgentMetricRecord.MetricReport report) {
        agentMetricService.reportMetric(report);
        return Response.accepted().build();
    }

    @POST
    @Path("/metrics/batch")
    @Operation(summary = "批量上报性能指标")
    @APIResponses({
        @APIResponse(responseCode = "202", description = "批量指标上报成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "batch", description = "批量性能指标上报数据", required = true)
    @RequiresPermission("agent:execute")
    public Response reportMetricsBatch(AgentMetricRecord.BatchMetricReport batch) {
        agentMetricService.reportMetricsBatch(batch);
        return Response.accepted().build();
    }

    @GET
    @Path("/metrics")
    @Operation(summary = "查询性能指标", description = "支持按 Agent ID、时间范围筛选，支持分页")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID（可选筛选条件）", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（默认 100）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @RequiresPermission("agent:view")
    public Response queryMetrics(
            @QueryParam("agentId") UUID agentId,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        var query = new AgentMetricRecord.MetricQuery(
                agentId,
                startTime != null ? parseDateTime(startTime) : null,
                endTime != null ? parseDateTime(endTime) : null,
                limit != null ? limit : 100,
                offset != null ? offset : 0
        );

        var result = agentMetricService.queryMetrics(query);
        return Response.ok(result).build();
    }

    @GET
    @Path("/metrics/{agentId}/latest")
    @Operation(summary = "获取最新性能指标")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回最新指标"),
        @APIResponse(responseCode = "404", description = "该 Agent 无指标数据"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getLatestMetric(@PathParam("agentId") UUID agentId) {
        var result = agentMetricService.getLatestMetric(agentId);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No metrics found for agent: " + agentId)
                    .build();
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/metrics/{agentId}/aggregation")
    @Operation(summary = "获取指标聚合数据", description = "默认返回最近 1 小时的聚合数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回聚合数据"),
        @APIResponse(responseCode = "404", description = "该 Agent 无指标数据"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式，默认 1 小时前）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式，默认当前时间）", required = false)
    @RequiresPermission("agent:view")
    public Response getMetricAggregation(
            @PathParam("agentId") UUID agentId,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {

        LocalDateTime start = startTime != null ? parseDateTime(startTime) : LocalDateTime.now().minusHours(1);
        LocalDateTime end = endTime != null ? parseDateTime(endTime) : LocalDateTime.now();

        var result = agentMetricService.getMetricAggregation(agentId, start, end);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No metrics found for agent: " + agentId)
                    .build();
        }
        return Response.ok(result).build();
    }

    @POST
    @Path("/snapshots/{agentId}")
    @Operation(summary = "创建状态快照")
    @APIResponses({
        @APIResponse(responseCode = "202", description = "快照创建成功"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response createSnapshot(@PathParam("agentId") UUID agentId) {
        agentMetricService.createStatusSnapshot(agentId);
        return Response.accepted().build();
    }

    @GET
    @Path("/snapshots")
    @Operation(summary = "查询状态快照", description = "支持按 Agent ID、时间范围筛选，支持分页")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回快照列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentId", description = "Agent 实例 ID（可选筛选条件）", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO 8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO 8601 格式）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（默认 100）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @RequiresPermission("agent:view")
    public Response querySnapshots(
            @QueryParam("agentId") UUID agentId,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        var query = new AgentMetricRecord.SnapshotQuery(
                agentId,
                startTime != null ? parseDateTime(startTime) : null,
                endTime != null ? parseDateTime(endTime) : null,
                limit != null ? limit : 100,
                offset != null ? offset : 0
        );

        var result = agentMetricService.querySnapshots(query);
        return Response.ok(result).build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "获取监控统计数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回监控统计"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("agent:view")
    public Response getMonitoringStats() {
        var result = agentMetricService.getMonitoringStats();
        return Response.ok(result).build();
    }

    @GET
    @Path("/status/batch")
    @Operation(summary = "批量获取 Agent 状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回 Agent 状态列表"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentIds", description = "Agent 实例 ID 列表（逗号分隔）", required = false)
    @RequiresPermission("agent:view")
    public Response getBatchStatus(@QueryParam("agentIds") String agentIds) {
        List<UUID> ids = null;
        if (agentIds != null && !agentIds.isEmpty()) {
            ids = Arrays.stream(agentIds.split(","))
                    .map(String::trim)
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }

        var result = agentMetricService.getBatchStatus(ids);
        return Response.ok(result).build();
    }

    @POST
    @Path("/alerts/trigger")
    @Operation(summary = "触发监控告警")
    @APIResponses({
        @APIResponse(responseCode = "202", description = "告警触发成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "request", description = "告警触发请求", required = true)
    @RequiresPermission("agent:execute")
    public Response triggerAlert(AgentMetricRecord.AlertTriggerRequest request) {
        agentMetricService.triggerAlert(request);
        return Response.accepted().build();
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
