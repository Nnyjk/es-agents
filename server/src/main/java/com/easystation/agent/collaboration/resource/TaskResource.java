package com.easystation.agent.collaboration.resource;

import com.easystation.agent.collaboration.dto.*;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * 协作任务 REST API
 */
@Path("/api/agent/collaboration/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    CollaborationService collaborationService;

    /**
     * 创建任务
     */
    @POST
    public Response createTask(CreateTaskRequest request,
                              @HeaderParam("X-Agent-ID") String agentId) {
        if (!request.validate()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: title is required\"}")
                    .build();
        }

        // Create task using the service interface
        AgentTaskDTO task = collaborationService.createTask(
            null, // sessionId - not available from request
            request.title,
            request.description,
            request.priority != null ? request.priority : "MEDIUM"
        );
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    /**
     * 获取任务详情
     */
    @GET
    @Path("/{taskId}")
    public Response getTask(@PathParam("taskId") Long taskId) {
        AgentTaskDTO task = collaborationService.getTask(taskId);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Task not found: " + taskId + "\"}")
                    .build();
        }
        return Response.ok(task).build();
    }

    /**
     * 获取会话的任务列表
     */
    @GET
    @Path("/session/{sessionId}")
    public Response getSessionTasks(@PathParam("sessionId") Long sessionId) {
        List<AgentTaskDTO> tasks = collaborationService.getSessionTasks(sessionId);
        return Response.ok(tasks).build();
    }

    /**
     * 按状态获取任务
     */
    @GET
    @Path("/status/{status}")
    public Response getTasksByStatus(@PathParam("status") String status) {
        List<AgentTaskDTO> tasks = collaborationService.getTasksByStatus(status);
        return Response.ok(tasks).build();
    }

    /**
     * 分配任务
     */
    @POST
    @Path("/{taskId}/assign")
    public Response assignTask(@PathParam("taskId") Long taskId,
                              TaskAssignmentRequest request) {
        if (!request.validate()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: taskId and agentId are required\"}")
                    .build();
        }
        AgentTaskDTO task = collaborationService.assignTask(taskId, request.agentId);
        return Response.ok(task).build();
    }

    /**
     * 更新任务状态
     */
    @PUT
    @Path("/{taskId}/status")
    public Response updateTaskStatus(@PathParam("taskId") Long taskId,
                                    String status) {
        if (status == null || status.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"status is required\"}")
                    .build();
        }
        AgentTaskDTO task = collaborationService.updateTaskStatus(taskId, status);
        return Response.ok(task).build();
    }

    /**
     * 完成任务
     */
    @POST
    @Path("/{taskId}/complete")
    public Response completeTask(@PathParam("taskId") Long taskId,
                                TaskCompletionResponse response) {
        if (response == null || response.taskId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request\"}")
                    .build();
        }
        AgentTaskDTO task = collaborationService.completeTask(taskId, response.result);
        return Response.ok(task).build();
    }

    /**
     * 任务失败
     */
    @POST
    @Path("/{taskId}/fail")
    public Response failTask(@PathParam("taskId") Long taskId,
                           String error) {
        AgentTaskDTO task = collaborationService.failTask(taskId, error);
        return Response.ok(task).build();
    }
}