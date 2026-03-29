package com.easystation.system.resource;

import com.easystation.system.record.ModuleActionRecord;
import com.easystation.system.record.ModuleRecord;
import com.easystation.system.service.ModuleService;
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

@Path("/modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "模块管理", description = "系统模块管理 API")
public class ModuleResource {

    @Inject
    ModuleService moduleService;

    @GET
    @Operation(summary = "获取模块列表", description = "查询所有系统模块列表")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回模块列表")
    })
    public Response list() {
        return Response.ok(moduleService.list()).build();
    }

    @POST
    @Operation(summary = "创建模块", description = "创建新的系统模块")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建模块"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response create(@Valid ModuleRecord dto) {
        return Response.status(Response.Status.CREATED).entity(moduleService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新模块", description = "更新指定模块的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新模块"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "模块不存在")
    })
    public Response update(@Parameter(description = "模块 ID") @PathParam("id") UUID id, ModuleRecord dto) {
        return Response.ok(moduleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除模块", description = "删除指定的系统模块")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除模块"),
        @APIResponse(responseCode = "404", description = "模块不存在")
    })
    public Response delete(@Parameter(description = "模块 ID") @PathParam("id") UUID id) {
        moduleService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/actions")
    @Operation(summary = "创建模块操作", description = "为模块创建新的操作项")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建模块操作"),
        @APIResponse(responseCode = "400", description = "请求参数无效")
    })
    public Response createAction(@Valid ModuleActionRecord dto) {
        return Response.status(Response.Status.CREATED).entity(moduleService.createAction(dto)).build();
    }

    @DELETE
    @Path("/actions/{id}")
    @Operation(summary = "删除模块操作", description = "删除指定的模块操作项")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除模块操作"),
        @APIResponse(responseCode = "404", description = "模块操作不存在")
    })
    public Response deleteAction(@Parameter(description = "模块操作 ID") @PathParam("id") UUID id) {
        moduleService.deleteAction(id);
        return Response.noContent().build();
    }
}
