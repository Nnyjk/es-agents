package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.AgentTaskStatus;
import com.easystation.agent.dto.AgentHealthRecord;
import com.easystation.agent.dto.AgentInstanceRecord;
import com.easystation.agent.dto.AgentRuntimeStatus;
import com.easystation.agent.record.AgentTaskRecord;
import com.easystation.agent.service.AgentInstanceService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/agents/instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentInstanceResource {

    @Inject
    AgentInstanceService agentInstanceService;

    @GET
    @RequiresPermission("agent:view")
    public Response list(@QueryParam("hostId") UUID hostId) {
        return Response.ok(agentInstanceService.list(hostId)).build();
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.get(id)).build();
    }

    @POST
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentInstanceRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentInstanceService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentInstanceRecord.Update dto) {
        return Response.ok(agentInstanceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentInstanceService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/commands")
    @RequiresPermission("agent:execute")
    public Response executeCommand(@PathParam("id") UUID id, @Valid AgentInstanceRecord.ExecuteCommand dto) {
        agentInstanceService.executeCommand(id, dto);
        return Response.accepted().build();
    }

    @POST
    @Path("/{id}/deploy")
    @RequiresPermission("agent:execute")
    public Response deploy(@PathParam("id") UUID id, @Valid AgentInstanceRecord.Deploy dto) {
        return Response.accepted()
                .entity(agentInstanceService.deploy(id, dto))
                .build();
    }

    @GET
    @Path("/{id}/tasks")
    @RequiresPermission("agent:view")
    public Response getTaskHistory(
            @PathParam("id") UUID id,
            @QueryParam("status") AgentTaskStatus status,
            @QueryParam("startTime") LocalDateTime startTime,
            @QueryParam("endTime") LocalDateTime endTime,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return Response.ok(agentInstanceService.queryTaskHistory(id, status, startTime, endTime, page, size)).build();
    }

    @GET
    @Path("/{id}/tasks/count")
    @RequiresPermission("agent:view")
    public Response countTaskHistory(
            @PathParam("id") UUID id,
            @QueryParam("status") AgentTaskStatus status,
            @QueryParam("startTime") LocalDateTime startTime,
            @QueryParam("endTime") LocalDateTime endTime) {
        return Response.ok(agentInstanceService.countTaskHistory(id, status, startTime, endTime)).build();
    }

    @GET
    @Path("/tasks/{taskId}")
    @RequiresPermission("agent:view")
    public Response getTaskDetail(@PathParam("taskId") UUID taskId) {
        return Response.ok(agentInstanceService.getTaskDetail(taskId)).build();
    }

    /**
     * 获取 Agent 实时运行状态
     */
    @GET
    @Path("/{id}/status")
    @RequiresPermission("agent:view")
    public Response getStatus(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.getRuntimeStatus(id)).build();
    }

    /**
     * 获取 Agent 健康度信息
     */
    @GET
    @Path("/{id}/health")
    @RequiresPermission("agent:view")
    public Response getHealth(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.getHealth(id)).build();
    }
}