package com.easystation.auth.resource;

import com.easystation.auth.dto.PermissionRecord;
import com.easystation.auth.service.PermissionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionResource {

    @Inject
    PermissionService permissionService;

    @GET
    public Response list(
            @QueryParam("keyword") String keyword,
            @QueryParam("resource") String resource,
            @QueryParam("action") String action,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        PermissionRecord.Query query = new PermissionRecord.Query(keyword, resource, action, limit, offset);
        return Response.ok(permissionService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(permissionService.get(id)).build();
    }

    @POST
    public Response create(@Valid PermissionRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(permissionService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid PermissionRecord.Update dto) {
        return Response.ok(permissionService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        permissionService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/check")
    public Response checkPermission(@Valid PermissionRecord.CheckRequest dto) {
        return Response.ok(permissionService.checkPermission(dto)).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getUserPermissions(@PathParam("userId") UUID userId) {
        return Response.ok(permissionService.getUserPermissions(userId)).build();
    }

    @POST
    @Path("/assign")
    public Response assignToRole(@Valid PermissionRecord.AssignPermissions dto) {
        permissionService.assignToRole(dto);
        return Response.ok().build();
    }

    @GET
    @Path("/role/{roleId}")
    public Response getRolePermissions(@PathParam("roleId") UUID roleId) {
        return Response.ok(permissionService.getRolePermissions(roleId)).build();
    }

    @POST
    @Path("/init")
    public Response initDefaultPermissions() {
        permissionService.initDefaultPermissions();
        return Response.ok().build();
    }
}