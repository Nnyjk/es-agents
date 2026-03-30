package com.easystation.agent.dto;

import java.time.Instant;
import java.util.List;

/**
 * 链路查询响应
 */
public record TraceQueryResponse(
    List<TraceSummary> traces
) {
    /**
     * 链路摘要信息
     */
    public record TraceSummary(
        String traceId,
        String serviceName,
        String operation,
        Instant startTime,
        long durationMs,
        boolean hasError
    ) {}
}
