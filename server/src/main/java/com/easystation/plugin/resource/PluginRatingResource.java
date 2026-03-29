package com.easystation.plugin.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.plugin.dto.PluginRatingRecord;
import com.easystation.plugin.service.PluginRatingService;
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

@Path("/api/v1/plugin-ratings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件评分管理", description = "插件用户评分 API")
public class PluginRatingResource {

    @Inject
    PluginRatingService ratingService;

    @POST
    @Operation(summary = "创建评分", description = "为插件创建评分")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "评分创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "404", description = "插件不存在")
    })
    @RequiresPermission("plugin:rate")
    public Response create(
            @Valid PluginRatingRecord.Create create,
            @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(ratingService.create(create, userId))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取评分", description = "根据 ID 查询评分详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回评分信息"),
        @APIResponse(responseCode = "404", description = "评分不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评分 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findById(@PathParam("id") UUID id) {
        return ratingService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/plugin/{pluginId}")
    @Operation(summary = "按插件查询评分", description = "查询指定插件的所有评分")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回评分列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(ratingService.findByPluginId(pluginId)).build();
    }

    @GET
    @Path("/plugin/{pluginId}/summary")
    @Operation(summary = "获取评分摘要", description = "获取插件的评分统计摘要（平均分、分布等）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回评分摘要"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response getSummary(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(ratingService.getSummary(pluginId)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新评分", description = "更新评分")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "评分更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "评分不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评分 ID", required = true)
    @RequiresPermission("plugin:rate")
    public Response update(@PathParam("id") UUID id, @Valid PluginRatingRecord.Update update) {
        return Response.ok(ratingService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除评分", description = "删除评分")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "评分删除成功"),
        @APIResponse(responseCode = "404", description = "评分不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评分 ID", required = true)
    @RequiresPermission("plugin:rate")
    public Response delete(@PathParam("id") UUID id) {
        ratingService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "搜索评分", description = "分页搜索评分记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回搜索结果"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:read")
    public Response search(@BeanParam PluginRatingRecord.Query query) {
        return Response.ok(ratingService.search(query)).build();
    }

    private UUID getUserId(SecurityContext securityContext) {
        String userIdStr = securityContext.getUserPrincipal().getName();
        return UUID.fromString(userIdStr);
    }
}
