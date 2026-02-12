package com.easystation.system.resource;

import com.easystation.system.domain.enums.UserStatus;
import com.easystation.system.record.UserRecord;
import com.easystation.system.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    public Response list() {
        return Response.ok(userService.list()).build();
    }

    @POST
    public Response create(@Valid UserRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(userService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, UserRecord.Update dto) {
        return Response.ok(userService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/status/{status}")
    public Response changeStatus(@PathParam("id") UUID id, @PathParam("status") UserStatus status) {
        return Response.ok(userService.changeStatus(id, status)).build();
    }
}
