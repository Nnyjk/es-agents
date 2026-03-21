package com.easystation.pipeline.resource;

import com.easystation.pipeline.dto.PipelineRecord;
import com.easystation.pipeline.enums.ExecutionStatus;
import com.easystation.pipeline.enums.PipelineStatus;
import com.easystation.pipeline.enums.TriggerType;
import com.easystation.pipeline.service.PipelineService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/pipelines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PipelineResource {

    @Inject
    PipelineService pipelineService;

    @GET
    public Response list(
            @QueryParam("status") PipelineStatus status,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("keyword") String keyword,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        PipelineRecord.Query query = new PipelineRecord.Query(status, environmentId, keyword, limit, offset);
        return Response.ok(pipelineService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(pipelineService.get(id)).build();
    }

    @POST
    public Response create(@Valid PipelineRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(pipelineService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid PipelineRecord.Update dto) {
        return Response.ok(pipelineService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        pipelineService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/execute")
    public Response execute(@PathParam("id") UUID id, PipelineRecord.ExecutionCreate dto) {
        PipelineRecord.ExecutionCreate execDto = new PipelineRecord.ExecutionCreate(
                id, dto.triggerType(), dto.triggeredBy(), dto.version()
        );
        return Response.status(Response.Status.CREATED)
                .entity(pipelineService.execute(execDto))
                .build();
    }

    @GET
    @Path("/{id}/executions")
    public Response listExecutions(
            @PathParam("id") UUID pipelineId,
            @QueryParam("status") ExecutionStatus status,
            @QueryParam("triggerType") TriggerType triggerType,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        PipelineRecord.ExecutionQuery query = new PipelineRecord.ExecutionQuery(
                pipelineId, status, triggerType, startTime, endTime, limit, offset
        );
        return Response.ok(pipelineService.listExecutions(query)).build();
    }

    @GET
    @Path("/executions/{executionId}")
    public Response getExecution(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.getExecution(executionId)).build();
    }

    @POST
    @Path("/executions/{executionId}/advance")
    public Response advanceStage(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.advanceStage(executionId)).build();
    }

    @POST
    @Path("/executions/{executionId}/cancel")
    public Response cancelExecution(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.cancelExecution(executionId)).build();
    }

    @GET
    @Path("/executions/{executionId}/stages")
    public Response getStages(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.getStages(executionId)).build();
    }
}