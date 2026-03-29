package com.easystation.agent.collaboration.resource;

import com.easystation.agent.collaboration.domain.AgentTask;
import com.easystation.agent.collaboration.dto.*;
import com.easystation.agent.collaboration.spi.CollaborationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

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
    public Response createTask(@QueryParam("sessionId") Long sessionId,
                              CreateTaskRequest request,
                              @HeaderParam("X-Agent-ID") String agentId) {
        if (!request.validate()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: title is required\"}")
                    .build();
        }

        AgentTask task = collaborationService.createTask(
                sessionId,
                request.title,
                request.description,
                request.taskType,
                request.priority,
                agentId != null ? agentId : "system",
                request.parameters
        );

        return Response.status(Response.Status.CREATED)
                .entity(AgentTaskDTO.fromEntity(task))
                .build();
    }

    /**
     * 获取任务详情
     */
    @GET
    @Path("/{taskId}")
    public Response getTask(@PathParam("taskId") Long taskId) {
        AgentTask task = collaborationService.getTask(taskId);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Task not found: " + taskId + "\"}")
                    .build();
        }
        return Response.ok(AgentTaskDTO.fromEntity(task)).build();
    }

    /**
     * 获取会话任务列表
     */
    @GET
    public Response getSessionTasks(@QueryParam("sessionId") Long sessionId,
                                   @QueryParam("status") String status) {
        List<AgentTask> tasks = collaborationService.getSessionTasks(sessionId, status);
        List<AgentTaskDTO> dtos = tasks.stream()
                .map(AgentTaskDTO::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    /**
     * 获取分配给 Agent 的任务
     */
    @GET
    @Path("/assigned")
    public Response getAssignedTasks(@QueryParam("agentId") String agentId,
                                    @QueryParam("status") String status) {
        List<AgentTask> tasks = collaborationService.getTasksByAgent(agentId, status);
        List<AgentTaskDTO> dtos = tasks.stream()
                .map(AgentTaskDTO::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
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
        collaborationService.assignTask(request.taskId != null ? request.taskId : taskId, request.agentId);
        return Response.noContent().build();
    }

    /**
     * 开始任务
     */
    @POST
    @Path("/{taskId}/start")
    public Response startTask(@PathParam("taskId") Long taskId) {
        collaborationService.startTask(taskId);
        return Response.noContent().build();
    }

    /**
     * 完成任务
     */
    @POST
    @Path("/{taskId}/complete")
    public Response completeTask(@PathParam("taskId") Long taskId,
                                TaskCompletionResponse request) {
        if (!request.validate()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid request: taskId is required\"}")
                    .build();
        }
        collaborationService.completeTask(
                request.taskId != null ? request.taskId : taskId,
                request.result
        );
        return Response.noContent().build();
    }

    /**
     * 失败任务
     */
    @POST
    @Path("/{taskId}/fail")
    public Response failTask(@PathParam("taskId") Long taskId,
                            @FormParam("error") String error) {
        collaborationService.failTask(taskId, error != null ? error : "Unknown error");
        return Response.noContent().build();
    }

    /**
     * 取消任务
     */
    @POST
    @Path("/{taskId}/cancel")
    public Response cancelTask(@PathParam("taskId") Long taskId) {
        collaborationService.cancelTask(taskId);
        return Response.noContent().build();
    }
}
