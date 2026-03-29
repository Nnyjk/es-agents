package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentTemplateRecord;
import com.easystation.agent.dto.AgentTemplateVersionRecord;
import com.easystation.agent.service.AgentTemplateService;
import com.easystation.agent.service.AgentTemplateVersionService;
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/agents/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 模板管理", description = "Agent 安装模板配置与版本管理 API")
public class AgentTemplateResource {

    @Inject
    AgentTemplateService agentTemplateService;

    @Inject
    AgentTemplateVersionService versionService;

    @GET
    @Operation(summary = "查询模板列表", description = "分页查询 Agent 模板")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回模板列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "osType", description = "按操作系统类型过滤", required = false)
    @Parameter(name = "sourceType", description = "按来源类型过滤", required = false)
    @Parameter(name = "category", description = "按分类过滤", required = false)
    @RequiresPermission("agent:view")
    public Response list(
            @QueryParam("osType") String osType,
            @QueryParam("sourceType") String sourceType,
            @QueryParam("category") String category
    ) {
        return Response.ok(agentTemplateService.list(osType, sourceType, category)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取模板详情", description = "获取指定 Agent 模板详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回模板详情"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentTemplateService.get(id)).build();
    }

    @GET
    @Path("/{id}/statistics")
    @Operation(summary = "获取模板统计", description = "获取模板使用统计信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getStatistics(@PathParam("id") UUID id) {
        return Response.ok(agentTemplateService.getStatistics(id)).build();
    }

    @GET
    @Path("/categories")
    @Operation(summary = "获取模板分类", description = "获取所有模板分类列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回分类列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:view")
    public Response listCategories() {
        return Response.ok(agentTemplateService.listCategories()).build();
    }

    @POST
    @Operation(summary = "创建模板", description = "创建新的 Agent 模板")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "模板创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentTemplateRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentTemplateService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新模板", description = "更新现有 Agent 模板")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "模板更新成功"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentTemplateRecord.Update dto) {
        return Response.ok(agentTemplateService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除模板", description = "删除指定 Agent 模板")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "模板删除成功"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentTemplateService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/download")
    @Operation(summary = "下载 Agent 包", description = "下载 Agent 安装包")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "下载成功"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RequiresPermission("agent:view")
    public Response download(@PathParam("id") UUID id) {
        String[] fileName = new String[1];
        InputStream is = agentTemplateService.download(id, fileName);

        return Response.ok(is)
                .header("Content-Disposition", "attachment; filename=\"" + fileName[0] + "\"")
                .build();
    }

    @GET
    @Path("/{id}/versions")
    @Operation(summary = "查询版本列表", description = "获取模板的所有版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回版本列表"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:view")
    public Response listVersions(@PathParam("id") UUID id) {
        agentTemplateService.get(id);
        return Response.ok(versionService.listByTemplate(id)).build();
    }

    @GET
    @Path("/{id}/versions/{versionId}")
    @Operation(summary = "获取版本详情", description = "获取指定版本详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回版本详情"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getVersion(@PathParam("id") UUID id, @PathParam("versionId") UUID versionId) {
        return Response.ok(versionService.getVersion(versionId)).build();
    }

    @POST
    @Path("/{id}/versions")
    @Operation(summary = "创建版本", description = "创建新的模板版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "版本创建成功"),
        @APIResponse(responseCode = "404", description = "模板不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @RequiresPermission("agent:edit")
    public Response createVersion(@PathParam("id") UUID id, @Valid AgentTemplateVersionRecord.Create dto) {
        return Response.ok(versionService.create(dto)).build();
    }

    @POST
    @Path("/{id}/versions/{versionId}/publish")
    @Operation(summary = "发布版本", description = "发布指定版本为当前版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "版本发布成功"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response publishVersion(@PathParam("id") UUID id, @PathParam("versionId") UUID versionId) {
        return Response.ok(versionService.publish(versionId, null)).build();
    }

    @POST
    @Path("/{id}/versions/{versionId}/rollback")
    @Operation(summary = "回滚版本", description = "回滚到指定版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "版本回滚成功"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "模板 ID", required = true)
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response rollbackToVersion(@PathParam("id") UUID id, @PathParam("versionId") UUID versionId) {
        return Response.ok(versionService.rollback(id, versionId)).build();
    }
}
