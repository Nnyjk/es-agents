package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentCommandRecord;
import com.easystation.agent.service.AgentCommandService;
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

import java.util.List;
import java.util.UUID;

@Path("/agents/commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 命令管理", description = "Agent 命令模板的 CRUD API")
public class AgentCommandResource {

    @Inject
    AgentCommandService agentCommandService;

    @GET
    @Operation(summary = "获取命令模板列表", description = "支持按模板 ID 筛选")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回命令模板列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "templateId", description = "命令模板 ID（可选筛选条件）", required = false)
    @RequiresPermission("agent:view")
    public List<AgentCommandRecord> list(@QueryParam("templateId") UUID templateId) {
        return agentCommandService.list(templateId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取单个命令模板详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回命令模板详情"),
        @APIResponse(responseCode = "404", description = "命令模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "命令模板 ID", required = true)
    @RequiresPermission("agent:view")
    public AgentCommandRecord get(@PathParam("id") UUID id) {
        return agentCommandService.get(id);
    }

    @POST
    @Operation(summary = "创建新的命令模板")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "成功创建命令模板"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentCommandRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentCommandService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新命令模板配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功更新命令模板"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "命令模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "命令模板 ID", required = true)
    @RequiresPermission("agent:edit")
    public AgentCommandRecord update(@PathParam("id") UUID id, @Valid AgentCommandRecord.Update dto) {
        return agentCommandService.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除命令模板")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功删除命令模板"),
        @APIResponse(responseCode = "404", description = "命令模板不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "命令模板 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentCommandService.delete(id);
        return Response.noContent().build();
    }
}
