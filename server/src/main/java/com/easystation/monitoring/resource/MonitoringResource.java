package com.easystation.monitoring.resource;

import com.easystation.monitoring.dto.MetricSummary;
import com.easystation.monitoring.dto.TimeSeriesData;
import com.easystation.monitoring.service.MonitoringService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * 监控指标 REST API
 * 提供系统监控指标的摘要和时序数据
 */
@Path("/monitoring")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "监控指标", description = "系统监控指标 API")
public class MonitoringResource {

    @Inject
    MonitoringService monitoringService;

    /**
     * 获取监控概览指标摘要
     */
    @GET
    @Path("/summary")
    @Operation(summary = "获取指标摘要", description = "获取系统监控指标摘要信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标摘要")
    })
    public Response getSummary() {
        MetricSummary summary = monitoringService.getMetricSummary();
        return Response.ok(summary).build();
    }

    /**
     * 获取时序数据
     */
    @GET
    @Path("/timeseries")
    @Operation(summary = "获取时序数据", description = "获取指定指标的时序数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回时序数据")
    })
    public Response getTimeseries(
            @QueryParam("metric") String metric,
            @QueryParam("timeRange") String timeRange,
            @QueryParam("start") String start,
            @QueryParam("end") String end) {
        List<TimeSeriesData> data = monitoringService.getTimeseriesData(metric, timeRange, start, end);
        return Response.ok(data).build();
    }

    /**
     * 获取指标趋势
     */
    @GET
    @Path("/metrics/trend")
    @Operation(summary = "获取指标趋势", description = "获取指定指标的趋势数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回指标趋势")
    })
    public Response getMetricTrend(
            @QueryParam("metric") String metric,
            @QueryParam("timeRange") String timeRange,
            @QueryParam("step") String step) {
        List<TimeSeriesData> data = monitoringService.getMetricTrend(metric, timeRange, step);
        return Response.ok(data).build();
    }
}