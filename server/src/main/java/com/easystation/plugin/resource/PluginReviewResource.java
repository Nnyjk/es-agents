package com.easystation.plugin.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.plugin.domain.enums.ReviewStatus;
import com.easystation.plugin.dto.PluginReviewRecord;
import com.easystation.plugin.service.PluginReviewService;
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

@Path("/api/v1/plugin-reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件审查管理", description = "插件提交审查与审核 API")
public class PluginReviewResource {

    @Inject
    PluginReviewService reviewService;

    @POST
    @Path("/submit")
    @Operation(summary = "提交审查", description = "提交插件进行审查")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "审查请求提交成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "404", description = "插件不存在")
    })
    @RequiresPermission("plugin:submit")
    public Response submit(@Valid PluginReviewRecord.Submit submit) {
        return Response.status(Response.Status.CREATED)
                .entity(reviewService.submit(submit))
                .build();
    }

    @POST
    @Path("/{reviewId}/approve")
    @Operation(summary = "批准审查", description = "批准插件审查请求")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "审查批准成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "审查记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "reviewId", description = "审查记录 ID", required = true)
    @RequiresPermission("plugin:admin")
    public Response approve(@PathParam("reviewId") UUID reviewId, @Valid PluginReviewRecord.Approve approve) {
        return Response.ok(reviewService.approve(reviewId, approve)).build();
    }

    @POST
    @Path("/{reviewId}/reject")
    @Operation(summary = "拒绝审查", description = "拒绝插件审查请求")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "审查拒绝成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "审查记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "reviewId", description = "审查记录 ID", required = true)
    @RequiresPermission("plugin:admin")
    public Response reject(@PathParam("reviewId") UUID reviewId, @Valid PluginReviewRecord.Reject reject) {
        return Response.ok(reviewService.reject(reviewId, reject)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取审查记录", description = "根据 ID 查询审查记录详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回审查记录"),
        @APIResponse(responseCode = "404", description = "审查记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "审查记录 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findById(@PathParam("id") UUID id) {
        return reviewService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/plugin/{pluginId}")
    @Operation(summary = "按插件查询审查", description = "查询指定插件的所有审查记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回审查列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(reviewService.findByPluginId(pluginId)).build();
    }

    @GET
    @Path("/status/{status}")
    @Operation(summary = "按状态查询审查", description = "按审查状态查询审查记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回审查列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "status", description = "审查状态（PENDING/APPROVED/REJECTED）", required = true)
    @RequiresPermission("plugin:read")
    public Response findByStatus(@PathParam("status") ReviewStatus status) {
        return Response.ok(reviewService.findByStatus(status)).build();
    }

    @GET
    @Path("/plugin/{pluginId}/pending")
    @Operation(summary = "查询待审记录", description = "查询指定插件的待审记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回待审记录"),
        @APIResponse(responseCode = "404", description = "未找到待审记录"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findPendingByPluginId(@PathParam("pluginId") UUID pluginId) {
        return reviewService.findPendingByPluginId(pluginId)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/search")
    @Operation(summary = "搜索审查记录", description = "分页搜索审查记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回搜索结果"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:read")
    public Response search(@BeanParam PluginReviewRecord.Query query) {
        return Response.ok(reviewService.search(query)).build();
    }

    @GET
    @Path("/count/status/{status}")
    @Operation(summary = "统计状态数量", description = "统计指定状态的审查记录数量")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回数量"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "status", description = "审查状态", required = true)
    @RequiresPermission("plugin:read")
    public Response countByStatus(@PathParam("status") ReviewStatus status) {
        return Response.ok(reviewService.countByStatus(status)).build();
    }

    @GET
    @Path("/count/plugin/{pluginId}")
    @Operation(summary = "统计插件审查数量", description = "统计指定插件的审查记录数量")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回数量"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "pluginId", description = "插件 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response countByPluginId(@PathParam("pluginId") UUID pluginId) {
        return Response.ok(reviewService.countByPluginId(pluginId)).build();
    }
}
