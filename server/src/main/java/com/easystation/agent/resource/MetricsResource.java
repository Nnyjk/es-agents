package com.easystation.agent.resource;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * 指标 REST 端点
 * 提供 Prometheus 格式指标和 JSON 摘要
 */
@Path("/api/v1/metrics")
@Startup
public class MetricsResource {

    @Inject
    MeterRegistry meterRegistry;

    /**
     * 获取 Prometheus 格式指标
     * 用于 Prometheus 抓取
     */
    @GET
    @Path("/prometheus")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "获取 Prometheus 格式指标")
    @APIResponse(responseCode = "200", description = "Prometheus 格式指标文本")
    @Tag(name = "metrics")
    public Response getPrometheusMetrics() {
        if (meterRegistry instanceof PrometheusMeterRegistry) {
            String metrics = ((PrometheusMeterRegistry) meterRegistry).scrape();
            return Response.ok(metrics).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("Prometheus registry not available")
                .build();
    }

    /**
     * 获取指标摘要（JSON 格式）
     * 用于前端展示
     */
    @GET
    @Path("/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "获取指标摘要")
    @APIResponse(responseCode = "200", description = "指标摘要 JSON")
    @Tag(name = "metrics")
    public Response getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // JVM 内存指标
        Map<String, Object> jvmMemory = new HashMap<>();
        meterRegistry.find("jvm.memory.used")
                .meters()
                .forEach(meter -> {
                    String area = meter.getId().getTag("area");
                    jvmMemory.put(area, meter.measure().iterator().next().getValue());
                });
        summary.put("jvmMemory", jvmMemory);

        // JVM 线程指标
        Map<String, Object> jvmThreads = new HashMap<>();
        meterRegistry.find("jvm.threads.live")
                .gauge()
                .ifPresent(gauge -> jvmThreads.put("live", gauge.value()));
        meterRegistry.find("jvm.threads.daemon")
                .gauge()
                .ifPresent(gauge -> jvmThreads.put("daemon", gauge.value()));
        summary.put("jvmThreads", jvmThreads);

        // CPU 指标
        Map<String, Object> cpu = new HashMap<>();
        meterRegistry.find("system.cpu.usage")
                .gauge()
                .ifPresent(gauge -> cpu.put("usage", gauge.value()));
        meterRegistry.find("system.cpu.count")
                .gauge()
                .ifPresent(gauge -> cpu.put("count", gauge.value()));
        summary.put("cpu", cpu);

        // Agent 指标
        Map<String, Object> agent = new HashMap<>();
        meterRegistry.find("esa.agent.count")
                .gauge()
                .ifPresent(gauge -> agent.put("count", gauge.value()));
        summary.put("agent", agent);

        // 任务指标
        Map<String, Object> tasks = new HashMap<>();
        meterRegistry.find("esa.task.execution.total")
                .counter()
                .ifPresent(counter -> tasks.put("executionTotal", counter.count()));
        meterRegistry.find("esa.task.success.total")
                .counter()
                .ifPresent(counter -> tasks.put("successTotal", counter.count()));
        meterRegistry.find("esa.task.failure.total")
                .counter()
                .ifPresent(counter -> tasks.put("failureTotal", counter.count()));
        summary.put("tasks", tasks);

        return Response.ok(summary).build();
    }
}
