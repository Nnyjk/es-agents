package com.easystation.plugin.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.plugin.dto.PluginCommentRecord;
import com.easystation.plugin.service.PluginCommentService;
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

@Path("/api/v1/plugin-comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件评论管理", description = "插件用户评论与回复管理 API")
public class PluginCommentResource {

    @Inject
    PluginCommentService commentService;

    @POST
    @Operation(summary = "创建评论", description = "为插件创建新评论")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "评论创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "404", description = "插件不存在")
    })
    @RequiresPermission("plugin:comment")
    public Response create(
            @Valid PluginCommentRecord.Create create,
            @Context SecurityContext securityContext) {
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(commentService.create(create, userId))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取评论", description = "根据 ID 查询评论详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回评论信息"),
        @APIResponse(responseCode = "404", description = "评论不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评论 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findById(@PathParam("id") UUID id) {
        return commentService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/plugin/{pluginId}")
    @Operation(summary = "按插件查询评论", description = "查询指定插件的所有评论")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回评论列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(commentService.findByPluginId(pluginId)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新评论", description = "更新评论内容")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "评论更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "评论不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评论 ID", required = true)
    @RequiresPermission("plugin:comment")
    public Response update(@PathParam("id") UUID id, @Valid PluginCommentRecord.Update update) {
        return Response.ok(commentService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除评论", description = "删除评论")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "评论删除成功"),
        @APIResponse(responseCode = "404", description = "评论不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评论 ID", required = true)
    @RequiresPermission("plugin:comment")
    public Response delete(@PathParam("id") UUID id) {
        commentService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/reply")
    @Operation(summary = "回复评论", description = "回复指定评论")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "回复创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "评论不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "被回复的评论 ID", required = true)
    @RequiresPermission("plugin:comment")
    public Response reply(
            @PathParam("id") UUID id,
            @Valid PluginCommentRecord.Create reply,
            @Context SecurityContext securityContext) {
        // Create a new comment as a reply
        PluginCommentRecord.Create replyCreate = new PluginCommentRecord.Create(
                reply.pluginId(),
                id, // parentId is the comment being replied to
                reply.replyToUserId(),
                reply.content()
        );
        UUID userId = getUserId(securityContext);
        return Response.status(Response.Status.CREATED)
                .entity(commentService.create(replyCreate, userId))
                .build();
    }

    @GET
    @Path("/{id}/replies")
    @Operation(summary = "获取回复列表", description = "获取指定评论的所有回复")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回回复列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "评论 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response getReplies(@PathParam("id") UUID id) {
        return Response.ok(commentService.findReplies(id)).build();
    }

    private UUID getUserId(SecurityContext securityContext) {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            try {
                return UUID.fromString(securityContext.getUserPrincipal().getName());
            } catch (IllegalArgumentException e) {
                // Return a default user ID for development/testing
                return UUID.randomUUID();
            }
        }
        // Return a default user ID for development/testing
        return UUID.randomUUID();
    }
}
