package com.easystation.config.resource;

import com.easystation.config.dto.ConfigRecord;
import com.easystation.config.service.ConfigService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigResource {

    @Inject
    ConfigService configService;

    @GET
    public Response list(
            @QueryParam("key") String key,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("group") String group,
            @QueryParam("active") Boolean active,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        ConfigRecord.Query query = new ConfigRecord.Query(key, environmentId, group, active, limit, offset);
        return Response.ok(configService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(configService.get(id)).build();
    }

    @GET
    @Path("/key/{key}")
    public Response getByKey(
            @PathParam("key") String key,
            @QueryParam("environmentId") UUID environmentId) {
        return Response.ok(configService.getByKey(key, environmentId)).build();
    }

    @POST
    public Response create(@Valid ConfigRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(configService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ConfigRecord.Update dto) {
        return Response.ok(configService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id, @QueryParam("deletedBy") String deletedBy) {
        configService.delete(id, deletedBy);
        return Response.noContent().build();
    }

    @POST
    @Path("/batch")
    public Response batchUpdate(@Valid ConfigRecord.BatchUpdate dto) {
        configService.batchUpdate(dto);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/history")
    public Response getHistory(
            @PathParam("id") UUID configId,
            @QueryParam("key") String key,
            @QueryParam("changeType") String changeType,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        ConfigRecord.HistoryQuery query = new ConfigRecord.HistoryQuery(configId, key, changeType, limit, offset);
        return Response.ok(configService.getHistory(query)).build();
    }

    @GET
    @Path("/history")
    public Response getAllHistory(
            @QueryParam("configId") UUID configId,
            @QueryParam("key") String key,
            @QueryParam("changeType") String changeType,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        ConfigRecord.HistoryQuery query = new ConfigRecord.HistoryQuery(configId, key, changeType, limit, offset);
        return Response.ok(configService.getHistory(query)).build();
    }

    @POST
    @Path("/history/{historyId}/rollback")
    public Response rollback(@PathParam("historyId") UUID historyId, @Valid ConfigRecord.RollbackRequest dto) {
        return Response.ok(configService.rollback(historyId, dto)).build();
    }

    @POST
    @Path("/environments")
    public Response getByEnvironments(List<UUID> environmentIds) {
        return Response.ok(configService.getByEnvironment(environmentIds)).build();
    }

    @GET
    @Path("/diff")
    public Response diff(
            @QueryParam("envId1") UUID envId1,
            @QueryParam("envId2") UUID envId2) {
        return Response.ok(configService.diff(envId1, envId2)).build();
    }

    @GET
    @Path("/groups")
    public Response listGroups() {
        return Response.ok(configService.listGroups()).build();
    }
}