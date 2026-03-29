package com.easystation.server.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统指标摘要 API
 * 提供业务指标摘要和 Prometheus 指标端点信息
 */
@Path("/api/v1/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "系统指标", description = "Prometheus 监控指标 API")
public class MetricsResource {

    @Inject
    CustomMetrics customMetrics;

    @Inject
    MeterRegistry meterRegistry;

    /**
     * 获取系统指标摘要
     */
    @GET
    @Path("/summary")
    @Operation(summary = "获取指标摘要", description = "获取系统业务指标摘要信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标摘要")
    })
    public Response getSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 业务指标
        summary.put("agents_online", customMetrics.getAgentsOnline());
        summary.put("active_users", customMetrics.getActiveUsers());

        // Prometheus 端点信息
        summary.put("prometheus_endpoint", "/q/metrics");
        summary.put("application", "easy-station-server");

        // JVM 内存信息
        Map<String, Object> jvm = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        jvm.put("max_memory", runtime.maxMemory());
        jvm.put("total_memory", runtime.totalMemory());
        jvm.put("free_memory", runtime.freeMemory());
        jvm.put("used_memory", runtime.totalMemory() - runtime.freeMemory());
        summary.put("jvm_memory", jvm);

        return Response.ok(summary).build();
    }

    /**
     * 获取 Prometheus 指标（文本格式）
     * 注：Prometheus 格式的指标由 Quarkus 在 /q/metrics 端点自动提供
     * 此方法提供 JSON 格式的指标概览
     */
    @GET
    @Path("/prometheus")
    @Operation(summary = "获取 Prometheus 指标概览", description = "获取 Prometheus 格式指标的 JSON 概览")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标概览"),
        @APIResponse(responseCode = "503", description = "Prometheus Registry 未启用")
    })
    public Response getPrometheusOverview() {
        if (meterRegistry instanceof PrometheusMeterRegistry) {
            Map<String, Object> overview = new HashMap<>();
            overview.put("prometheus_endpoint", "/q/metrics");
            overview.put("metrics_available", meterRegistry.getMeters().size());
            overview.put("scrape_format", "text/plain; version=0.0.4");

            // 添加可用的指标名称
            overview.put("metric_names", meterRegistry.getMeters().stream()
                    .map(meter -> meter.getId().getName())
                    .distinct()
                    .sorted()
                    .toList());

            return Response.ok(overview).build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("error", "Prometheus Registry 未启用"))
                .build();
    }

    /**
     * 获取指标列表
     */
    @GET
    @Path("/list")
    @Operation(summary = "获取指标列表", description = "获取所有注册的指标名称列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标列表")
    })
    public Response getMetricList() {
        return Response.ok(meterRegistry.getMeters().stream()
                .map(meter -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", meter.getId().getName());
                    info.put("type", meter.getId().getType().name());
                    info.put("description", meter.getId().getDescription());
                    return info;
                })
                .toList())
                .build();
    }
}