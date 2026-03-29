package com.easystation.plugin.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.plugin.dto.PluginRecord;
import com.easystation.plugin.dto.PluginVersionRecord;
import com.easystation.plugin.service.PluginService;
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

import java.util.UUID;

@Path("/api/v1/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件管理", description = "插件发布、版本管理与市场 API")
public class PluginResource {

    @Inject
    PluginService pluginService;

    @POST
    @Operation(summary = "创建插件", description = "发布新的插件")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "插件创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "create", description = "插件创建数据", required = true)
    @RequiresPermission("plugin:write")
    public Response create(@Valid PluginRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(pluginService.create(create))
                .build();
    }

    @GET
    @Operation(summary = "列出插件", description = "分页列出所有插件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回插件列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "page", description = "页码（从 0 开始，默认 0）", required = false)
    @Parameter(name = "size", description = "每页数量（默认 20）", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "category", description = "按分类过滤", required = false)
    @RequiresPermission("plugin:read")
    public Response list(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("keyword") String keyword,
            @QueryParam("category") String category) {
        PluginRecord.Query query = new PluginRecord.Query();
        query.setKeyword(keyword);
        query.setPage(page != null ? page : 0);
        query.setSize(size != null ? size : 20);
        if (category != null && !category.isEmpty()) {
            try {
                query.setCategoryId(java.util.UUID.fromString(category));
            } catch (IllegalArgumentException ignored) {
                // 忽略无效的 UUID
            }
        }
        return Response.ok(pluginService.search(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取插件详情", description = "根据 ID 获取插件详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回插件详情"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response get(@PathParam("id") UUID id) {
        return pluginService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新插件", description = "更新插件基本信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "插件更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "插件 ID", required = true)
    @Parameter(name = "update", description = "插件更新数据", required = true)
    @RequiresPermission("plugin:write")
    public Response update(@PathParam("id") UUID id, @Valid PluginRecord.Update update) {
        return Response.ok(pluginService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除插件", description = "删除指定的插件")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "插件删除成功"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "插件 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response delete(@PathParam("id") UUID id) {
        pluginService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "搜索插件", description = "根据关键词搜索插件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回搜索结果"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "keyword", description = "搜索关键词", required = true)
    @Parameter(name = "limit", description = "返回数量限制（默认 20）", required = false)
    @RequiresPermission("plugin:read")
    public Response search(
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit) {
        PluginRecord.Query query = new PluginRecord.Query();
        query.setKeyword(keyword);
        query.setSize(limit != null ? limit : 20);
        return Response.ok(pluginService.search(query)).build();
    }

    @POST
    @Path("/{pluginId}/versions")
    @Operation(summary = "创建插件版本", description = "为插件发布新版本")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "版本创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @Parameter(name = "create", description = "版本创建数据", required = true)
    @RequiresPermission("plugin:write")
    public Response createVersion(
            @PathParam("pluginId") UUID pluginId,
            @Valid PluginVersionRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(pluginService.createVersion(pluginId, create))
                .build();
    }

    @GET
    @Path("/{pluginId}/versions")
    @Operation(summary = "列出插件版本", description = "获取插件的所有版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回版本列表"),
        @APIResponse(responseCode = "404", description = "插件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response listVersions(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(pluginService.findVersionsByPluginId(pluginId)).build();
    }

    @GET
    @Path("/{pluginId}/versions/latest")
    @Operation(summary = "获取最新版本", description = "获取插件的最新版本")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回最新版本"),
        @APIResponse(responseCode = "404", description = "插件不存在或无版本"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response getLatestVersion(@PathParam("pluginId") UUID pluginId) {
        return pluginService.findLatestVersion(pluginId)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/versions/{versionId}")
    @Operation(summary = "获取版本详情", description = "根据版本 ID 获取版本详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回版本详情"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response getVersionById(@PathParam("versionId") UUID versionId) {
        return pluginService.findVersionById(versionId)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/versions/{versionId}")
    @Operation(summary = "更新版本", description = "更新版本信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "版本更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @Parameter(name = "update", description = "版本更新数据", required = true)
    @RequiresPermission("plugin:write")
    public Response updateVersion(
            @PathParam("versionId") UUID versionId,
            @Valid PluginVersionRecord.Update update) {
        return Response.ok(pluginService.updateVersion(versionId, update)).build();
    }

    @DELETE
    @Path("/versions/{versionId}")
    @Operation(summary = "删除版本", description = "删除指定的插件版本")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "版本删除成功"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response deleteVersion(@PathParam("versionId") UUID versionId) {
        pluginService.deleteVersion(versionId);
        return Response.noContent().build();
    }

    @POST
    @Path("/versions/{versionId}/publish")
    @Operation(summary = "发布版本", description = "将版本状态改为已发布")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "版本发布成功"),
        @APIResponse(responseCode = "400", description = "版本无法发布（状态不允许）"),
        @APIResponse(responseCode = "404", description = "版本不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "versionId", description = "版本 ID", required = true)
    @RequiresPermission("plugin:write")
    public Response publishVersion(@PathParam("versionId") UUID versionId) {
        return Response.ok(pluginService.publishVersion(versionId)).build();
    }
}
