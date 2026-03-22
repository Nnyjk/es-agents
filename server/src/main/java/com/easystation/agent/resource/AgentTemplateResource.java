package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentTemplateRecord;
import com.easystation.agent.service.AgentTemplateService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/agents/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentTemplateResource {

    @Inject
    AgentTemplateService agentTemplateService;

    /**
     * 列表查询模板
     */
    @GET
    public Response list(
            @QueryParam("osType") String osType,
            @QueryParam("sourceType") String sourceType,
            @QueryParam("category") String category
    ) {
        return Response.ok(agentTemplateService.list(osType, sourceType, category)).build();
    }

    /**
     * 获取模板详情
     */
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentTemplateService.get(id)).build();
    }

    /**
     * 获取模板使用统计
     */
    @GET
    @Path("/{id}/statistics")
    public Response getStatistics(@PathParam("id") UUID id) {
        return Response.ok(agentTemplateService.getStatistics(id)).build();
    }

    /**
     * 获取所有模板分类
     */
    @GET
    @Path("/categories")
    public Response listCategories() {
        return Response.ok(agentTemplateService.listCategories()).build();
    }

    /**
     * 创建模板
     */
    @POST
    public Response create(@Valid AgentTemplateRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentTemplateService.create(dto))
                .build();
    }

    /**
     * 更新模板
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AgentTemplateRecord.Update dto) {
        return Response.ok(agentTemplateService.update(id, dto)).build();
    }

    /**
     * 删除模板
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        agentTemplateService.delete(id);
        return Response.noContent().build();
    }

    /**
     * 下载Agent包
     * 提供统一的下载入口，根据模板配置的来源类型处理下载逻辑
     */
    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("id") UUID id) {
        String[] fileName = new String[1];
        InputStream is = agentTemplateService.download(id, fileName);

        return Response.ok(is)
                .header("Content-Disposition", "attachment; filename=\"" + fileName[0] + "\"")
                .build();
    }
}