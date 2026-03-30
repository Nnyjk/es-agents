package com.easystation.diagnostic.resource;

import com.easystation.diagnostic.dto.DiagnosticRuleRecord;
import com.easystation.diagnostic.service.DiagnosticRuleService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * 诊断规则 REST API
 */
@Path("/api/diagnostic/rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DiagnosticRuleResource {

    @Inject
    DiagnosticRuleService ruleService;

    /**
     * 列出所有规则
     */
    @GET
    public List<DiagnosticRuleRecord.Summary> list() {
        return ruleService.listAll();
    }

    /**
     * 按分类列出规则
     */
    @GET
    @Path("/category/{category}")
    public List<DiagnosticRuleRecord.Summary> listByCategory(@PathParam("category") String category) {
        return ruleService.listByCategory(category);
    }

    /**
     * 获取规则详情
     */
    @GET
    @Path("/{ruleId}")
    public DiagnosticRuleRecord.Detail get(@PathParam("ruleId") String ruleId) {
        return ruleService.get(ruleId);
    }

    /**
     * 创建规则
     */
    @POST
    public Response create(@Valid DiagnosticRuleRecord.Create request) {
        DiagnosticRuleRecord.Detail created = ruleService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * 更新规则
     */
    @PUT
    @Path("/{ruleId}")
    public DiagnosticRuleRecord.Detail update(
            @PathParam("ruleId") String ruleId,
            @Valid DiagnosticRuleRecord.Update request) {
        return ruleService.update(ruleId, request);
    }

    /**
     * 启用规则
     */
    @POST
    @Path("/{ruleId}/enable")
    public Response enable(@PathParam("ruleId") String ruleId) {
        ruleService.enable(ruleId);
        return Response.ok().build();
    }

    /**
     * 禁用规则
     */
    @POST
    @Path("/{ruleId}/disable")
    public Response disable(@PathParam("ruleId") String ruleId) {
        ruleService.disable(ruleId);
        return Response.ok().build();
    }

    /**
     * 删除规则
     */
    @DELETE
    @Path("/{ruleId}")
    public Response delete(@PathParam("ruleId") String ruleId) {
        ruleService.delete(ruleId);
        return Response.noContent().build();
    }
}
