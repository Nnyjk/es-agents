package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentRepositoryRecord;
import com.easystation.agent.service.AgentRepositoryService;
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

import java.util.UUID;

@Path("/agents/repositories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 仓库管理", description = "Agent 代码仓库的 CRUD API")
public class AgentRepositoryResource {

    @Inject
    AgentRepositoryService agentRepositoryService;

    @GET
    @Operation(summary = "获取仓库列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回仓库列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("agent:view")
    public Response list() {
        return Response.ok(agentRepositoryService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取单个仓库详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回仓库详情"),
        @APIResponse(responseCode = "404", description = "仓库不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "仓库 ID", required = true)
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentRepositoryService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建新的仓库")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "成功创建仓库"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "dto", description = "仓库创建请求", required = true)
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentRepositoryRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
            .entity(agentRepositoryService.create(dto))
            .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新仓库配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功更新仓库"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "仓库不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "仓库 ID", required = true)
    @Parameter(name = "dto", description = "仓库更新请求", required = true)
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentRepositoryRecord.Update dto) {
        return Response.ok(agentRepositoryService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除仓库")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功删除仓库"),
        @APIResponse(responseCode = "404", description = "仓库不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "仓库 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentRepositoryService.delete(id);
        return Response.noContent().build();
    }
}
