package com.easystation.system.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.system.record.RoleRecord;
import com.easystation.system.service.RoleService;
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

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "角色管理", description = "角色管理 API")
public class RoleResource {

    @Inject
    RoleService roleService;

    @GET
    @Operation(summary = "获取角色列表", description = "查询所有角色列表")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回角色列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("role:view")
    public Response list() {
        return Response.ok(roleService.list()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取角色详情", description = "根据 ID 查询角色详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回角色详情"),
        @APIResponse(responseCode = "404", description = "角色不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("role:view")
    public Response get(@Parameter(description = "角色 ID") @PathParam("id") UUID id) {
        return Response.ok(roleService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建角色", description = "创建新的角色")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建角色"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "角色名称已存在")
    })
    @RequiresPermission("role:create")
    public Response create(@Valid RoleRecord dto) {
        return Response.status(Response.Status.CREATED).entity(roleService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新角色", description = "更新指定角色的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新角色"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "角色不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("role:edit")
    public Response update(@Parameter(description = "角色 ID") @PathParam("id") UUID id, RoleRecord dto) {
        return Response.ok(roleService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除角色", description = "删除指定的角色")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除角色"),
        @APIResponse(responseCode = "404", description = "角色不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("role:delete")
    public Response delete(@Parameter(description = "角色 ID") @PathParam("id") UUID id) {
        roleService.delete(id);
        return Response.noContent().build();
    }
}
