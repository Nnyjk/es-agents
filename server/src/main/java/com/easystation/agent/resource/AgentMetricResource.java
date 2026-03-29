package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentMetricRecord;
import com.easystation.agent.service.AgentMetricService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agent 监控 API
 */
@Path("/agents/monitoring")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentMetricResource {

    @Inject
    AgentMetricService agentMetricService;

    /**
     * 上报性能指标
     */
    @POST
    @Path("/metrics")
    @RequiresPermission("agent:execute")
    public Response reportMetric(AgentMetricRecord.MetricReport report) {
        agentMetricService.reportMetric(report);
        return Response.accepted().build();
    }

    /**
     * 批量上报性能指标
     */
    @POST
    @Path("/metrics/batch")
    @RequiresPermission("agent:execute")
    public Response reportMetricsBatch(AgentMetricRecord.BatchMetricReport batch) {
        agentMetricService.reportMetricsBatch(batch);
        return Response.accepted().build();
    }

    /**
     * 查询性能指标
     */
    @GET
    @Path("/metrics")
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

    /**
     * 获取最新性能指标
     */
    @GET
    @Path("/metrics/{agentId}/latest")
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

    /**
     * 获取指标聚合数据
     */
    @GET
    @Path("/metrics/{agentId}/aggregation")
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

    /**
     * 创建状态快照
     */
    @POST
    @Path("/snapshots/{agentId}")
    @RequiresPermission("agent:execute")
    public Response createSnapshot(@PathParam("agentId") UUID agentId) {
        agentMetricService.createStatusSnapshot(agentId);
        return Response.accepted().build();
    }

    /**
     * 查询状态快照
     */
    @GET
    @Path("/snapshots")
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

    /**
     * 获取监控统计数据
     */
    @GET
    @Path("/stats")
    @RequiresPermission("agent:view")
    public Response getMonitoringStats() {
        var result = agentMetricService.getMonitoringStats();
        return Response.ok(result).build();
    }

    /**
     * 批量获取 Agent 状态
     */
    @GET
    @Path("/status/batch")
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

    /**
     * 触发监控告警
     */
    @POST
    @Path("/alerts/trigger")
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