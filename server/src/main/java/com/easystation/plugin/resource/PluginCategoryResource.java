package com.easystation.plugin.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.plugin.dto.PluginCategoryRecord;
import com.easystation.plugin.service.PluginCategoryService;
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

@Path("/api/v1/plugin-categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "插件分类管理", description = "插件分类体系管理 API")
public class PluginCategoryResource {

    @Inject
    PluginCategoryService categoryService;

    @POST
    @Operation(summary = "创建分类", description = "创建新的插件分类")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "分类创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:admin")
    public Response create(@Valid PluginCategoryRecord.Create create) {
        return Response.status(Response.Status.CREATED)
                .entity(categoryService.create(create))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取分类", description = "根据 ID 查询插件分类")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回分类信息"),
        @APIResponse(responseCode = "404", description = "分类不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "分类 ID", required = true)
    @RequiresPermission("plugin:read")
    public Response findById(@PathParam("id") UUID id) {
        return categoryService.findById(id)
                .map(Response::ok)
                .map(Response.ResponseBuilder::build)
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Operation(summary = "列出所有分类", description = "获取所有插件分类列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回分类列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:read")
    public Response findAll() {
        return Response.ok(categoryService.findAll()).build();
    }

    @GET
    @Path("/tree")
    @Operation(summary = "获取分类树", description = "获取插件分类的层级树结构")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回分类树"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("plugin:read")
    public Response getTree() {
        return Response.ok(categoryService.getCategoryTree()).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新分类", description = "更新插件分类信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "分类更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "分类不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "分类 ID", required = true)
    @RequiresPermission("plugin:admin")
    public Response update(@PathParam("id") UUID id, @Valid PluginCategoryRecord.Update update) {
        return Response.ok(categoryService.update(id, update)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除分类", description = "删除插件分类")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "分类删除成功"),
        @APIResponse(responseCode = "404", description = "分类不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "分类 ID", required = true)
    @RequiresPermission("plugin:admin")
    public Response delete(@PathParam("id") UUID id) {
        categoryService.delete(id);
        return Response.noContent().build();
    }
}
