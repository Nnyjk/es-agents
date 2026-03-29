package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentCredentialRecord;
import com.easystation.agent.service.AgentCredentialService;
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

@Path("/agents/credentials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 凭证管理", description = "Agent 凭证（SSH/API Key）的 CRUD API")
public class AgentCredentialResource {

    @Inject
    AgentCredentialService agentCredentialService;

    @GET
    @Operation(summary = "获取凭证列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回凭证列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("agent:view")
    public Response list() {
        return Response.ok(agentCredentialService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取单个凭证详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回凭证详情"),
        @APIResponse(responseCode = "404", description = "凭证不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "凭证 ID", required = true)
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentCredentialService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建新的凭证")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "成功创建凭证"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "dto", description = "凭证创建请求", required = true)
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentCredentialRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
            .entity(agentCredentialService.create(dto))
            .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新凭证配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功更新凭证"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "凭证不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "凭证 ID", required = true)
    @Parameter(name = "dto", description = "凭证更新请求", required = true)
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentCredentialRecord.Update dto) {
        return Response.ok(agentCredentialService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除凭证")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功删除凭证"),
        @APIResponse(responseCode = "404", description = "凭证不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "凭证 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentCredentialService.delete(id);
        return Response.noContent().build();
    }
}
