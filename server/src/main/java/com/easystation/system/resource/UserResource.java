package com.easystation.system.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.system.domain.enums.UserStatus;
import com.easystation.system.record.UserRecord;
import com.easystation.system.service.UserService;
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

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "用户管理", description = "用户管理 API")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(summary = "获取用户列表", description = "查询所有用户列表")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回用户列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("user:view")
    public Response list() {
        return Response.ok(userService.list()).build();
    }

    @POST
    @Operation(summary = "创建用户", description = "创建新的用户")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建用户"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "用户名已存在")
    })
    @RequiresPermission("user:create")
    public Response create(@Valid UserRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(userService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新用户", description = "更新指定用户的信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新用户"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "用户不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("user:edit")
    public Response update(@Parameter(description = "用户 ID") @PathParam("id") UUID id, UserRecord.Update dto) {
        return Response.ok(userService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除用户", description = "删除指定的用户")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除用户"),
        @APIResponse(responseCode = "404", description = "用户不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("user:delete")
    public Response delete(@Parameter(description = "用户 ID") @PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/status/{status}")
    @Operation(summary = "更改用户状态", description = "更改指定用户的状态（启用/禁用）")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更改用户状态"),
        @APIResponse(responseCode = "404", description = "用户不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("user:edit")
    public Response changeStatus(
            @Parameter(description = "用户 ID") @PathParam("id") UUID id,
            @Parameter(description = "用户状态") @PathParam("status") UserStatus status) {
        return Response.ok(userService.changeStatus(id, status)).build();
    }
}
