package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentSourceRecord;
import com.easystation.agent.dto.ValidationResult;
import com.easystation.agent.service.AgentSourceService;
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
import java.util.UUID;

@Path("/agents/sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 源码管理", description = "Agent 源码版本、下载、测试 API")
public class AgentSourceResource {

    @Inject
    AgentSourceService agentSourceService;

    @GET
    @Operation(summary = "获取源码列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回源码列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("agent:view")
    public Response list() {
        return Response.ok(agentSourceService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取单个源码详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回源码详情"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "源码 ID", required = true)
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentSourceService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建新的源码")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "成功创建源码"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "dto", description = "源码创建请求", required = true)
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentSourceRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentSourceService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新源码配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功更新源码"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "源码 ID", required = true)
    @Parameter(name = "dto", description = "源码更新请求", required = true)
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentSourceRecord.Update dto) {
        return Response.ok(agentSourceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除源码")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功删除源码"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "源码 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentSourceService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/download")
    @Operation(summary = "下载源码文件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回源码文件"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "源码 ID", required = true)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RequiresPermission("agent:view")
    public Response download(@PathParam("id") UUID id) {
        String[] fileName = new String[1];
        InputStream is = agentSourceService.getSourceStream(id, fileName);
        
        return Response.ok(is)
                .header("Content-Disposition", "attachment; filename=\"" + fileName[0] + "\"")
                .build();
    }

    @POST
    @Path("/{id}/test")
    @Operation(summary = "测试源码可用性")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回测试结果"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "源码 ID", required = true)
    @RequiresPermission("agent:view")
    public Response test(@PathParam("id") UUID id) {
        ValidationResult result = agentSourceService.testSource(id);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}/metadata")
    @Operation(summary = "获取源码元数据")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回源码元数据"),
        @APIResponse(responseCode = "404", description = "源码不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "源码 ID", required = true)
    @RequiresPermission("agent:view")
    public Response metadata(@PathParam("id") UUID id) {
        return Response.ok(agentSourceService.getSourceMetadata(id)).build();
    }
}
