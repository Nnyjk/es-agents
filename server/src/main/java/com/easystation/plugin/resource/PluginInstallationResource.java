package com.easystation.plugin.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.plugin.dto.PluginInstallationRecord;
import com.easystation.plugin.service.PluginInstallationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/plugin-installations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件安装管理", description = "插件安装、配置、启停管理 API")
public class PluginInstallationResource {

    @Inject
    PluginInstallationService installationService;

    @POST
    @Operation(summary = "安装插件", description = "安装单个插件到指定 Agent")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "插件安装成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "404", description = "插件或 Agent 不存在")
    })
    @RequiresPermission("plugin:install")
    public Response install(
            @Valid PluginInstallationRecord.Install install,
            @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(installationService.install(install, userId))
                .build();
    }

    @POST
    @Path("/batch")
    @Operation(summary = "批量安装插件", description = "批量安装多个插件到指定 Agent")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "插件批量安装成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:install")
    public Response batchInstall(
            @Valid PluginInstallationRecord.BatchInstall batchInstall,
            @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(installationService.batchInstall(batchInstall, userId))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取安装记录", description = "根据 ID 查询插件安装记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回安装记录"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findById(@PathParam("id") UUID id) {
        return installationService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/agent/{agentId}")
    @Operation(summary = "按 Agent 查询", description = "查询指定 Agent 的所有插件安装记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回安装列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "agentId", description = "Agent ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findByAgentId(@PathParam("agentId") UUID agentId) {
        return Response.ok(installationService.findByAgentId(agentId)).build();
    }

    @GET
    @Path("/plugin/{pluginId}")
    @Operation(summary = "按插件查询", description = "查询指定插件的所有安装记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回安装列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(installationService.findByPluginId(pluginId)).build();
    }

    @PUT
    @Path("/{id}/config")
    @Operation(summary = "更新配置", description = "更新插件安装配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "配置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response updateConfig(
            @PathParam("id") UUID id,
            @Valid PluginInstallationRecord.UpdateConfig update) {
        return Response.ok(installationService.updateConfig(id, update)).build();
    }

    @PUT
    @Path("/{id}/start")
    @Operation(summary = "启动插件", description = "启动已安装的插件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "插件启动成功"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response start(@PathParam("id") UUID id) {
        return Response.ok(installationService.start(id)).build();
    }

    @PUT
    @Path("/{id}/stop")
    @Operation(summary = "停止插件", description = "停止运行中的插件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "插件停止成功"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response stop(@PathParam("id") UUID id) {
        return Response.ok(installationService.stop(id)).build();
    }

    @PUT
    @Path("/{id}/enable")
    @Operation(summary = "启用插件", description = "启用已禁用的插件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "插件启用成功"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response enable(@PathParam("id") UUID id) {
        return Response.ok(installationService.enable(id)).build();
    }

    @PUT
    @Path("/{id}/disable")
    @Operation(summary = "禁用插件", description = "禁用运行中的插件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "插件禁用成功"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response disable(@PathParam("id") UUID id) {
        return Response.ok(installationService.disable(id)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "卸载插件", description = "卸载已安装的插件")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "插件卸载成功"),
        @APIResponse(responseCode = "404", description = "安装记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "安装记录 ID", required = true)
    @RequiresPermission("plugin:delete")
    public Response uninstall(@PathParam("id") UUID id) {
        installationService.uninstall(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "搜索安装记录", description = "分页搜索插件安装记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回搜索结果"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:read")
    public Response search(@BeanParam PluginInstallationRecord.Query query) {
        return Response.ok(installationService.search(query)).build();
    }

    private UUID getUserId(SecurityContext securityContext) {
        String userIdStr = securityContext.getUserPrincipal().getName();
        return UUID.fromString(userIdStr);
    }
}
