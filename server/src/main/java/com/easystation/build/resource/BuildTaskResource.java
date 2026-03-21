package com.easystation.build.resource;

import com.easystation.build.dto.BuildRecord;
import com.easystation.build.enums.BuildStatus;
import com.easystation.build.enums.BuildType;
import com.easystation.build.service.BuildTaskService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/builds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildTaskResource {

    @Inject
    BuildTaskService buildTaskService;

    @GET
    public Response list(
            @QueryParam("type") BuildType type,
            @QueryParam("status") BuildStatus status,
            @QueryParam("templateId") UUID templateId,
            @QueryParam("keyword") String keyword,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        BuildRecord.Query query = new BuildRecord.Query(
                type, status, templateId, keyword, startTime, endTime, limit, offset
        );
        return Response.ok(buildTaskService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(buildTaskService.get(id)).build();
    }

    @POST
    public Response create(@Valid BuildRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(buildTaskService.create(dto))
                .build();
    }

    @POST
    @Path("/{id}/start")
    public Response start(@PathParam("id") UUID id) {
        return Response.ok(buildTaskService.start(id)).build();
    }

    @POST
    @Path("/{id}/cancel")
    public Response cancel(@PathParam("id") UUID id) {
        return Response.ok(buildTaskService.cancel(id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        buildTaskService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/artifacts")
    public Response listArtifacts(@PathParam("id") UUID taskId) {
        return Response.ok(buildTaskService.listArtifacts(taskId)).build();
    }

    @POST
    @Path("/{id}/artifacts")
    public Response createArtifact(@PathParam("id") UUID taskId) {
        return Response.status(Response.Status.CREATED)
                .entity(buildTaskService.createArtifact(taskId))
                .build();
    }

    @GET
    @Path("/counts")
    public Response counts() {
        return Response.ok(Map.of(
                "pending", buildTaskService.countByStatus(BuildStatus.PENDING),
                "running", buildTaskService.countByStatus(BuildStatus.RUNNING),
                "success", buildTaskService.countByStatus(BuildStatus.SUCCESS),
                "failed", buildTaskService.countByStatus(BuildStatus.FAILED),
                "cancelled", buildTaskService.countByStatus(BuildStatus.CANCELLED)
        )).build();
    }

    @GET
    @Path("/types")
    public Response getBuildTypes() {
        List<TypeInfo> types = List.of(BuildType.values()).stream()
                .map(t -> new TypeInfo(t.name(), t.name().replace("_", " ").toLowerCase()))
                .toList();
        return Response.ok(types).build();
    }

    record TypeInfo(String name, String description) {}
}