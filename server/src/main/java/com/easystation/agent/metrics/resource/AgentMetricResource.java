package com.easystation.agent.metrics.resource;

import com.easystation.agent.metrics.dto.MetricRecord;
import com.easystation.agent.metrics.enums.MetricType;
import com.easystation.agent.metrics.service.AgentMetricService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/agents/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 指标管理", description = "Agent 性能指标上报与查询 API")
public class AgentMetricResource {

    @Inject
    AgentMetricService metricService;

    @POST
    @Path("/report")
    @Operation(summary = "上报指标", description = "Agent 上报性能指标数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "指标上报成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:execute")
    public Response report(@Valid MetricRecord.Report dto) {
        metricService.report(dto);
        return Response.ok().build();
    }

    @GET
    @Operation(summary = "查询指标", description = "分页查询指标数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "按 Agent ID 过滤", required = false)
    @Parameter(name = "hostId", description = "按主机 ID 过滤", required = false)
    @Parameter(name = "types", description = "按指标类型过滤 (逗号分隔)", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
    @RequiresPermission("agent:view")
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

    @GET
    @Path("/hosts/{hostId}/summary")
    @Operation(summary = "获取主机指标摘要", description = "获取主机性能指标摘要")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回主机指标摘要"),
        @APIResponse(responseCode = "404", description = "主机无指标数据"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "hostId", description = "主机 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getHostSummary(@PathParam("hostId") UUID hostId) {
        MetricRecord.HostMetricsSummary summary = metricService.getHostSummary(hostId);
        if (summary == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No metrics found for host"))
                    .build();
        }
        return Response.ok(summary).build();
    }

    @GET
    @Path("/agents/{agentId}/summary")
    @Operation(summary = "获取 Agent 指标摘要", description = "获取 Agent 进程指标摘要")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回 Agent 指标摘要"),
        @APIResponse(responseCode = "404", description = "Agent 无指标数据"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @RequiresPermission("agent:view")
    public Response getAgentSummary(@PathParam("agentId") UUID agentId) {
        MetricRecord.AgentMetricsSummary summary = metricService.getAgentSummary(agentId);
        if (summary == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No metrics found for agent"))
                    .build();
        }
        return Response.ok(summary).build();
    }

    @GET
    @Path("/agents/{agentId}/history/{type}")
    @Operation(summary = "获取指标历史", description = "获取 Agent 指标历史趋势")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标历史"),
        @APIResponse(responseCode = "404", description = "无历史数据"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @Parameter(name = "type", description = "指标类型", required = true)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "intervalMinutes", description = "采样间隔 (分钟)", required = false)
    @RequiresPermission("agent:view")
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

    @GET
    @Path("/types")
    @Operation(summary = "获取指标类型", description = "获取所有支持的指标类型")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标类型列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:view")
    public Response getMetricTypes() {
        List<MetricTypeInfo> types = List.of(MetricType.values()).stream()
                .map(t -> new MetricTypeInfo(t.name(), t.getDescription(), t.getUnit()))
                .toList();
        return Response.ok(types).build();
    }

    @DELETE
    @Path("/expired")
    @Operation(summary = "删除过期指标", description = "清理过期指标数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "删除成功"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "retentionDays", description = "保留天数", required = false)
    @RequiresPermission("agent:delete")
    public Response deleteExpired(@QueryParam("retentionDays") @DefaultValue("30") int retentionDays) {
        long deleted = metricService.deleteExpired(retentionDays);
        return Response.ok(Map.of("deleted", deleted)).build();
    }

    record MetricTypeInfo(String name, String description, String unit) {}
}
