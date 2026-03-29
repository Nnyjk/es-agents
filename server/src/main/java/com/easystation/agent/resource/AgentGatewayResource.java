package com.easystation.agent.resource;

import com.easystation.agent.dto.HeartbeatRequest;
import com.easystation.agent.service.AgentInstanceService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/gateway")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 网关 API", description = "Agent 心跳上报、命令拉取 API")
public class AgentGatewayResource {

    @Inject
    AgentInstanceService agentInstanceService;

    @POST
    @Path("/heartbeat")
    @Operation(summary = "Agent 心跳上报")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "心跳上报成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "认证失败"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "X-Agent-Secret", description = "Agent 密钥（Header 传递）", required = true)
    @Parameter(name = "request", description = "心跳请求数据", required = true)
    @RequiresPermission("agent:execute")
    public Response heartbeat(
            @HeaderParam("X-Agent-Secret") String secretKey,
            HeartbeatRequest request) {
        agentInstanceService.handleHeartbeat(request, secretKey);
        return Response.ok().build();
    }

    @GET
    @Path("/commands")
    @Operation(summary = "拉取待执行命令")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回待执行命令列表"),
        @APIResponse(responseCode = "401", description = "认证失败"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "X-Agent-Secret", description = "Agent 密钥（Header 传递）", required = true)
    @Parameter(name = "agentId", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response fetchCommands(
            @HeaderParam("X-Agent-Secret") String secretKey,
            @QueryParam("agentId") String agentId) {
        return Response.ok(agentInstanceService.fetchPendingTasks(agentId, secretKey)).build();
    }
}
