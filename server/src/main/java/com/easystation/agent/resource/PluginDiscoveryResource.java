package com.easystation.agent.resource;

import com.easystation.agent.dto.PluginInfoDTO;
import com.easystation.agent.service.PluginDiscoveryService;
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

import java.util.List;
import java.util.Map;

@Path("/api/v1/agents/{agentId}/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件发现管理", description = "Agent 插件发现与状态管理 API")
public class PluginDiscoveryResource {

    @Inject
    PluginDiscoveryService pluginDiscoveryService;

    @GET
    @Operation(summary = "查询插件列表", description = "获取 Agent 的所有插件列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回插件列表"),
        @APIResponse(responseCode = "404", description = "Agent 不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @RequiresPermission("agent:view")
    public Response listPlugins(@PathParam("agentId") String agentId) {
        List<PluginInfoDTO> plugins = pluginDiscoveryService.getPlugins(agentId);
        return Response.ok(plugins).build();
    }

    @GET
    @Path("/{pluginId}")
    @Operation(summary = "获取插件详情", description = "获取指定插件的详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回插件详情"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getPlugin(@PathParam("agentId") String agentId, @PathParam("pluginId") String pluginId) {
        return pluginDiscoveryService.getPlugin(agentId, pluginId)
            .map(Response::ok)
            .orElse(Response.status(Response.Status.NOT_FOUND))
            .build();
    }

    @PUT
    @Path("/{pluginId}/status")
    @Operation(summary = "更新插件状态", description = "更新插件的运行状态 (启用/停用)")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "状态更新成功"),
        @APIResponse(responseCode = "400", description = "状态参数无效"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @Parameter(name = "status", description = "插件状态 (RUNNING/STOPPED/ERROR)", required = true)
    @RequiresPermission("agent:edit")
    public Response updateStatus(
        @PathParam("agentId") String agentId,
        @PathParam("pluginId") String pluginId,
        String status
    ) {
        if (status == null || status.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Status is required"))
                .build();
        }

        if (!List.of("RUNNING", "STOPPED", "ERROR").contains(status)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid status: " + status))
                .build();
        }

        pluginDiscoveryService.updatePluginStatus(agentId, pluginId, status);
        return Response.ok().build();
    }

    @GET
    @Path("/count")
    @Operation(summary = "获取运行中插件数量", description = "获取 Agent 的运行中插件数量")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回数量"),
        @APIResponse(responseCode = "404", description = "Agent 不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @RequiresPermission("agent:view")
    public Response countRunningPlugins(@PathParam("agentId") String agentId) {
        long count = pluginDiscoveryService.countRunningPlugins(agentId);
        return Response.ok(count).build();
    }
}
