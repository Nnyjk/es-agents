package com.easystation.diagnostic.resource;

import com.easystation.diagnostic.dto.DiagnosticReportRecord;
import com.easystation.diagnostic.service.DiagnosticReportService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * 诊断报告 REST API
 */
@Path("/api/diagnostic/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiagnosticReportResource {

    @Inject
    DiagnosticReportService reportService;

    /**
     * 列出所有报告
     */
    @GET
    public List<DiagnosticReportRecord.Summary> list() {
        return reportService.list();
    }

    /**
     * 列出最近的报告
     */
    @GET
    @Path("/recent")
    public List<DiagnosticReportRecord.Summary> listRecent(
            @QueryParam("limit") @DefaultValue("10") int limit) {
        return reportService.listRecent(limit);
    }

    /**
     * 获取报告详情（包含发现列表）
     */
    @GET
    @Path("/{reportId}")
    public DiagnosticReportRecord.WithFindings get(@PathParam("reportId") String reportId) {
        return reportService.get(reportId);
    }

    /**
     * 生成诊断报告（同步）
     */
    @POST
    public Response generate(@Valid DiagnosticReportRecord.Generate request) {
        DiagnosticReportRecord.Detail report = reportService.generate(request);
        return Response.status(Response.Status.CREATED).entity(report).build();
    }

    /**
     * 生成诊断报告（异步）
     */
    @POST
    @Path("/async")
    public Response generateAsync(@Valid DiagnosticReportRecord.Generate request) {
        DiagnosticReportRecord.Detail report = reportService.generateAsync(request);
        return Response.status(Response.Status.ACCEPTED).entity(report).build();
    }

    /**
     * 删除报告
     */
    @DELETE
    @Path("/{reportId}")
    public Response delete(@PathParam("reportId") String reportId) {
        reportService.delete(reportId);
        return Response.noContent().build();
    }
}
