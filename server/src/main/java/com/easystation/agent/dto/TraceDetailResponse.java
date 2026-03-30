package com.easystation.agent.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 链路详情响应
 */
public record TraceDetailResponse(
    String traceId,
    List<SpanData> spans
) {
    /**
     * Span 数据
     */
    public record SpanData(
        String traceId,
        String spanId,
        String parentSpanId,
        String serviceName,
        String operationName,
        String spanKind,
        Instant startTime,
        long durationMs,
        boolean hasError,
        List<Map.Entry<String, String>> attributes
    ) {}
}
