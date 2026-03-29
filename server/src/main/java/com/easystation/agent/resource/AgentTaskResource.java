package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.AgentTaskStatus;
import com.easystation.agent.record.AgentTaskRecord;
import com.easystation.agent.record.TaskRecord;
import com.easystation.agent.service.AgentTaskService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/agents/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 任务管理", description = "Agent 任务的创建、查询、重试、取消 API")
public class AgentTaskResource {

    @Inject
    AgentTaskService agentTaskService;

    @POST
    @Path("/execute")
    @Operation(summary = "在 Agent 实例上执行命令")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "request", description = "命令执行请求", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public Response executeCommand(
            @Valid TaskRecord.ExecuteRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        AgentTaskRecord task = agentTaskService.execute(
                request.agentInstanceId(),
                request.commandId(),
                request.args(),
                username
        );
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    @POST
    @Path("/execute-script")
    @Operation(summary = "在 Agent 实例上直接执行脚本")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "request", description = "脚本执行请求", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public Response executeScript(
            @Valid TaskRecord.ExecuteScriptRequest request,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        AgentTaskRecord task = agentTaskService.executeScript(
                request.agentInstanceId(),
                request.script(),
                request.timeout(),
                username
        );
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "根据 ID 获取任务详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务详情"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public AgentTaskRecord getById(@PathParam("id") UUID id) {
        return agentTaskService.get(id);
    }

    @GET
    @Operation(summary = "查询任务列表", description = "支持按 Agent 实例 ID 和状态筛选")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "agentInstanceId", description = "Agent 实例 ID（可选筛选条件）", required = false)
    @Parameter(name = "status", description = "任务状态筛选（可选）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（默认 50）", required = false)
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public List<AgentTaskRecord> list(
            @QueryParam("agentInstanceId") UUID agentInstanceId,
            @QueryParam("status") AgentTaskStatus status,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        if (agentInstanceId != null) {
            return agentTaskService.listByAgentInstance(agentInstanceId, status, limit);
        } else {
            return agentTaskService.listRecent(limit);
        }
    }

    @POST
    @Path("/{id}/retry")
    @Operation(summary = "重试失败的任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务重试成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public AgentTaskRecord retry(
            @PathParam("id") UUID id,
            @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal() != null 
                ? securityContext.getUserPrincipal().getName() 
                : "system";
        return agentTaskService.retry(id, username);
    }

    @POST
    @Path("/{id}/cancel")
    @Operation(summary = "取消待处理的任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务取消成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public AgentTaskRecord cancel(@PathParam("id") UUID id) {
        return agentTaskService.cancel(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除任务记录")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "任务删除成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin"})
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentTaskService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/counts")
    @Operation(summary = "获取任务状态统计")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回各状态任务数量"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public TaskRecord.TaskCounts getCounts() {
        Map<AgentTaskStatus, Long> counts = agentTaskService.countByStatus();
        return new TaskRecord.TaskCounts(
                counts.getOrDefault(AgentTaskStatus.PENDING, 0L),
                counts.getOrDefault(AgentTaskStatus.SENT, 0L),
                counts.getOrDefault(AgentTaskStatus.RUNNING, 0L),
                counts.getOrDefault(AgentTaskStatus.SUCCESS, 0L),
                counts.getOrDefault(AgentTaskStatus.FAILED, 0L)
        );
    }
}
