package com.easystation.auth.resource;

import com.easystation.auth.model.Permission;
import com.easystation.auth.service.PermissionService;
import com.easystation.common.permission.RequirePermission;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * 权限管理 API
 */
@Path("/api/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionResource {

    @Inject
    PermissionService permissionService;

    /**
     * 获取所有权限
     */
    @GET
    @RequirePermission(resource = "permission", action = "read")
    public Response getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return Response.ok(permissions).build();
    }

    /**
     * 根据资源类型获取权限
     */
    @GET
    @Path("/resource/{resource}")
    @RequirePermission(resource = "permission", action = "read")
    public Response getPermissionsByResource(@PathParam("resource") String resource) {
        List<Permission> permissions = permissionService.getPermissionsByResource(resource);
        return Response.ok(permissions).build();
    }

    /**
     * 创建权限
     */
    @POST
    @RequirePermission(resource = "permission", action = "create")
    public Response createPermission(PermissionDTO dto) {
        Permission permission = permissionService.createPermission(
            dto.code,
            dto.name,
            dto.resource,
            dto.action,
            Permission.DataScope.valueOf(dto.dataScope),
            dto.description
        );
        return Response.status(Response.Status.CREATED).entity(permission).build();
    }

    /**
     * 删除权限
     */
    @DELETE
    @Path("/{id}")
    @RequirePermission(resource = "permission", action = "delete")
    public Response deletePermission(@PathParam("id") UUID id) {
        boolean deleted = permissionService.deletePermission(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * 权限 DTO
     */
    public static class PermissionDTO {
        public String code;
        public String name;
        public String resource;
        public String action;
        public String dataScope = "ALL";
        public String description;
    }
}
