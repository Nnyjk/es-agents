package com.easystation.scheduler.resource;

import com.easystation.scheduler.dto.ScheduledTaskRecord;
import com.easystation.scheduler.service.ScheduledTaskService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/scheduled-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduledTaskResource {

    @Inject
    ScheduledTaskService taskService;

    @GET
    public Response list(
            @QueryParam("keyword") String keyword,
            @QueryParam("type") String type,
            @QueryParam("status") String status,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        ScheduledTaskRecord.Query query = new ScheduledTaskRecord.Query(
                keyword,
                type != null ? com.easystation.scheduler.enums.TaskType.valueOf(type) : null,
                status != null ? com.easystation.scheduler.enums.TaskStatus.valueOf(status) : null,
                limit, offset
        );
        return Response.ok(taskService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(taskService.get(id)).build();
    }

    @POST
    public Response create(@Valid ScheduledTaskRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(taskService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid ScheduledTaskRecord.Update dto) {
        return Response.ok(taskService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        taskService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/enable")
    public Response enable(@PathParam("id") UUID id) {
        return Response.ok(taskService.enable(id)).build();
    }

    @POST
    @Path("/{id}/disable")
    public Response disable(@PathParam("id") UUID id) {
        return Response.ok(taskService.disable(id)).build();
    }

    @POST
    @Path("/{id}/execute")
    public Response executeNow(@PathParam("id") UUID id, @Valid ScheduledTaskRecord.ExecuteRequest dto) {
        return Response.ok(taskService.executeNow(id, dto)).build();
    }

    @GET
    @Path("/{id}/executions")
    public Response getExecutions(
            @PathParam("id") UUID taskId,
            @QueryParam("status") String status,
            @QueryParam("triggerType") String triggerType,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        ScheduledTaskRecord.ExecutionQuery query = new ScheduledTaskRecord.ExecutionQuery(
                taskId,
                status != null ? com.easystation.scheduler.enums.ExecutionStatus.valueOf(status) : null,
                triggerType, startTime, endTime, limit, offset
        );
        return Response.ok(taskService.getExecutions(query)).build();
    }

    @GET
    @Path("/executions/{executionId}")
    public Response getExecution(@PathParam("executionId") UUID executionId) {
        return Response.ok(taskService.getExecution(executionId)).build();
    }

    @GET
    @Path("/executions")
    public Response getAllExecutions(
            @QueryParam("taskId") UUID taskId,
            @QueryParam("status") String status,
            @QueryParam("triggerType") String triggerType,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
        ScheduledTaskRecord.ExecutionQuery query = new ScheduledTaskRecord.ExecutionQuery(
                taskId,
                status != null ? com.easystation.scheduler.enums.ExecutionStatus.valueOf(status) : null,
                triggerType, startTime, endTime, limit, offset
        );
        return Response.ok(taskService.getExecutions(query)).build();
    }

    @POST
    @Path("/validate-cron")
    public Response validateCron(@QueryParam("cron") String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ScheduledTaskRecord.CronValidation(false, "Cron expression is required", null))
                    .build();
        }
        return Response.ok(taskService.validateCron(cronExpression)).build();
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        return Response.ok(taskService.getStats()).build();
    }
}