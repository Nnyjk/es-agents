package com.easystation.auth.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.auth.dto.PermissionRecord;
import com.easystation.auth.service.PermissionService;
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

@Path("/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "权限管理", description = "权限管理 API")
public class PermissionResource {

    @Inject
    PermissionService permissionService;

    @GET
    @Operation(summary = "获取权限列表", description = "分页查询权限列表，支持按关键字、资源、操作筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回权限列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:view")
    public Response list(
            @Parameter(description = "关键字（模糊匹配）") @QueryParam("keyword") String keyword,
            @Parameter(description = "资源名称") @QueryParam("resource") String resource,
            @Parameter(description = "操作类型") @QueryParam("action") String action,
            @Parameter(description = "返回数量限制") @QueryParam("limit") Integer limit,
            @Parameter(description = "偏移量") @QueryParam("offset") Integer offset) {
        PermissionRecord.Query query = new PermissionRecord.Query(keyword, resource, action, limit, offset);
        return Response.ok(permissionService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取权限详情", description = "根据 ID 查询权限详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回权限详情"),
        @APIResponse(responseCode = "404", description = "权限不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:view")
    public Response get(@Parameter(description = "权限 ID") @PathParam("id") UUID id) {
        return Response.ok(permissionService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建权限", description = "创建新的权限项")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建权限"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:create")
    public Response create(@Valid PermissionRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(permissionService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新权限", description = "更新指定权限的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新权限"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "权限不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:edit")
    public Response update(@Parameter(description = "权限 ID") @PathParam("id") UUID id, @Valid PermissionRecord.Update dto) {
        return Response.ok(permissionService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除权限", description = "删除指定的权限项")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除权限"),
        @APIResponse(responseCode = "404", description = "权限不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:delete")
    public Response delete(@Parameter(description = "权限 ID") @PathParam("id") UUID id) {
        permissionService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/check")
    @Operation(summary = "检查权限", description = "检查用户是否拥有指定权限")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "返回权限检查结果"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:view")
    public Response checkPermission(@Valid PermissionRecord.CheckRequest dto) {
        return Response.ok(permissionService.checkPermission(dto)).build();
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回用户权限列表"),
        @APIResponse(responseCode = "404", description = "用户不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:view")
    public Response getUserPermissions(@Parameter(description = "用户 ID") @PathParam("userId") UUID userId) {
        return Response.ok(permissionService.getUserPermissions(userId)).build();
    }

    @POST
    @Path("/assign")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功分配权限"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:assign")
    public Response assignToRole(@Valid PermissionRecord.AssignPermissions dto) {
        permissionService.assignToRole(dto);
        return Response.ok().build();
    }

    @GET
    @Path("/role/{roleId}")
    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回角色权限列表"),
        @APIResponse(responseCode = "404", description = "角色不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:view")
    public Response getRolePermissions(@Parameter(description = "角色 ID") @PathParam("roleId") UUID roleId) {
        return Response.ok(permissionService.getRolePermissions(roleId)).build();
    }

    @POST
    @Path("/init")
    @Operation(summary = "初始化默认权限", description = "初始化系统默认权限项")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功初始化默认权限"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("permission:manage")
    public Response initDefaultPermissions() {
        permissionService.initDefaultPermissions();
        return Response.ok().build();
    }
}