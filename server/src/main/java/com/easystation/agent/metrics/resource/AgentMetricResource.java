package com.easystation.agent.metrics.resource;

import com.easystation.agent.metrics.dto.MetricRecord;
import com.easystation.agent.metrics.enums.MetricType;
import com.easystation.agent.metrics.service.AgentMetricService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/agents/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentMetricResource {

    @Inject
    AgentMetricService metricService;

    /**
     * 上报指标数据
     */
    @POST
    @Path("/report")
    public Response report(@Valid MetricRecord.Report dto) {
        metricService.report(dto);
        return Response.ok().build();
    }

    /**
     * 查询指标列表
     */
    @GET
    public Response list(
            @QueryParam("agentId") UUID agentId,
            @QueryParam("hostId") UUID hostId,
            @QueryParam("types") List<MetricType> types,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        MetricRecord.Query query = new MetricRecord.Query(
                agentId, hostId, types, startTime, endTime, limit, offset
        );
        return Response.ok(metricService.list(query)).build();
    }

    /**
     * 获取主机指标摘要
     */
    @GET
    @Path("/hosts/{hostId}/summary")
    public Response getHostSummary(@PathParam("hostId") UUID hostId) {
        MetricRecord.HostMetricsSummary summary = metricService.getHostSummary(hostId);
        if (summary == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No metrics found for host"))
                    .build();
        }
        return Response.ok(summary).build();
    }

    /**
     * 获取 Agent 进程指标摘要
     */
    @GET
    @Path("/agents/{agentId}/summary")
    public Response getAgentSummary(@PathParam("agentId") UUID agentId) {
        MetricRecord.AgentMetricsSummary summary = metricService.getAgentSummary(agentId);
        if (summary == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No metrics found for agent"))
                    .build();
        }
        return Response.ok(summary).build();
    }

    /**
     * 获取指标历史趋势
     */
    @GET
    @Path("/agents/{agentId}/history/{type}")
    public Response getMetricHistory(
            @PathParam("agentId") UUID agentId,
            @PathParam("type") MetricType type,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("interval") Integer intervalMinutes) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        MetricRecord.MetricHistory history = metricService.getMetricHistory(
                agentId, type, startTime, endTime, intervalMinutes
        );
        return Response.ok(history).build();
    }

    /**
     * 获取支持的指标类型
     */
    @GET
    @Path("/types")
    public Response getMetricTypes() {
        List<MetricTypeInfo> types = List.of(MetricType.values()).stream()
                .map(t -> new MetricTypeInfo(t.name(), t.getDescription(), t.getUnit()))
                .toList();
        return Response.ok(types).build();
    }

    /**
     * 删除过期指标数据
     */
    @DELETE
    @Path("/expired")
    public Response deleteExpired(@QueryParam("retentionDays") @DefaultValue("30") int retentionDays) {
        long deleted = metricService.deleteExpired(retentionDays);
        return Response.ok(Map.of("deleted", deleted)).build();
    }

    record MetricTypeInfo(String name, String description, String unit) {}
}