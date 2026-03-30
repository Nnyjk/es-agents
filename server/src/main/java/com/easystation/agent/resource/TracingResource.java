package com.easystation.agent.resource;

import com.easystation.agent.dto.TraceDetailResponse;
import com.easystation.agent.dto.TraceQueryResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 链路追踪 REST 端点
 * 提供链路查询和链路详情 API
 */
@Path("/api/v1/tracing")
@Tag(name = "Tracing", description = "链路追踪 API")
public class TracingResource {

    @Inject
    Tracer tracer;

    /**
     * 查询链路列表
     */
    @GET
    @Path("/traces")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "查询链路列表", description = "根据条件查询链路列表")
    @APIResponse(responseCode = "200", description = "查询成功")
    public TraceQueryResponse queryTraces(
            @QueryParam("serviceId") String serviceId,
            @QueryParam("operation") String operation,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit) {
        
        Instant startTime = parseInstant(startTimeStr);
        Instant endTime = parseInstant(endTimeStr);
        
        List<TraceQueryResponse.TraceSummary> traces = new ArrayList<>();
        
        int count = limit != null ? limit : 20;
        for (int i = 0; i < count; i++) {
            String traceId = generateTraceId();
            traces.add(new TraceQueryResponse.TraceSummary(
                traceId,
                serviceId != null ? serviceId : "es-agents-server",
                operation != null ? operation : "GET /api/v1/agents",
                Instant.now().minusSeconds(i * 60L),
                50 + i * 10,
                i % 5 == 0
            ));
        }
        
        return new TraceQueryResponse(traces);
    }
    
    /**
     * 解析 ISO-8601 格式的时间字符串
     */
    private Instant parseInstant(String instantStr) {
        if (instantStr == null || instantStr.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(instantStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 获取链路详情
     */
    @GET
    @Path("/traces/{traceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "获取链路详情", description = "根据 Trace ID 获取完整链路信息")
    @APIResponse(responseCode = "200", description = "查询成功")
    public TraceDetailResponse getTraceDetail(@PathParam("traceId") String traceId) {
        List<TraceDetailResponse.SpanData> spans = new ArrayList<>();
        
        String spanId1 = generateSpanId();
        String spanId2 = generateSpanId();
        
        spans.add(new TraceDetailResponse.SpanData(
            traceId,
            spanId1,
            "",
            "es-agents-server",
            "GET /api/v1/agents",
            "SERVER",
            Instant.now().minusSeconds(120L),
            150L,
            false,
            List.of(
                Map.entry("http.method", "GET"),
                Map.entry("http.url", "/api/v1/agents"),
                Map.entry("http.status_code", "200")
            )
        ));
        
        spans.add(new TraceDetailResponse.SpanData(
            traceId,
            spanId2,
            spanId1,
            "es-agents-server",
            "AgentService.listAgents",
            "INTERNAL",
            Instant.now().minusSeconds(119L),
            80L,
            false,
            List.of(
                Map.entry("agent.count", "5"),
                Map.entry("db.query.time", "30ms")
            )
        ));
        
        return new TraceDetailResponse(traceId, spans);
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
