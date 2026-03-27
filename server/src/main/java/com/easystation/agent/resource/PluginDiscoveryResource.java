package com.easystation.agent.resource;

import com.easystation.agent.dto.PluginInfoDTO;
import com.easystation.agent.service.PluginDiscoveryService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/agents/{agentId}/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginDiscoveryResource {

    @Inject
    PluginDiscoveryService pluginDiscoveryService;

    /**
     * 获取 Agent 的所有插件列表
     */
    @GET
    @RequiresPermission("agent:view")
    public Response listPlugins(@PathParam("agentId") String agentId) {
        List<PluginInfoDTO> plugins = pluginDiscoveryService.getPlugins(agentId);
        return Response.ok(plugins).build();
    }

    /**
     * 获取 Agent 的特定插件详情
     */
    @GET
    @Path("/{pluginId}")
    @RequiresPermission("agent:view")
    public Response getPlugin(@PathParam("agentId") String agentId, @PathParam("pluginId") String pluginId) {
        return pluginDiscoveryService.getPlugin(agentId, pluginId)
            .map(Response::ok)
            .orElse(Response.status(Response.Status.NOT_FOUND))
            .build();
    }

    /**
     * 更新插件状态（启用/停用）
     */
    @PUT
    @Path("/{pluginId}/status")
    @RequiresPermission("agent:edit")
    public Response updateStatus(
        @PathParam("agentId") String agentId,
        @PathParam("pluginId") String pluginId,
        String status
    ) {
        if (status == null || status.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Status is required")
                .build();
        }

        // 验证状态值
        if (!List.of("RUNNING", "STOPPED", "ERROR").contains(status)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid status: " + status)
                .build();
        }

        pluginDiscoveryService.updatePluginStatus(agentId, pluginId, status);
        return Response.ok().build();
    }

    /**
     * 获取 Agent 的运行中插件数量
     */
    @GET
    @Path("/count")
    @RequiresPermission("agent:view")
    public Response countRunningPlugins(@PathParam("agentId") String agentId) {
        long count = pluginDiscoveryService.countRunningPlugins(agentId);
        return Response.ok(count).build();
    }
}