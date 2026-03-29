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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@Path("/agents/instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 实例管理", description = "Agent 实例的 CRUD、命令执行、部署和任务管理 API")
public class AgentInstanceResource {

    @Inject
    AgentInstanceService agentInstanceService;

    @GET
    @Operation(summary = "获取 Agent 实例列表", description = "支持按主机 ID 筛选")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回 Agent 实例列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "hostId", description = "主机 ID（可选筛选条件）", required = false)
    @RequiresPermission("agent:view")
    public Response list(@QueryParam("hostId") UUID hostId) {
        return Response.ok(agentInstanceService.list(hostId)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取单个 Agent 实例详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回 Agent 实例详情"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建新的 Agent 实例")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "成功创建 Agent 实例"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(@Valid AgentInstanceRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(agentInstanceService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新 Agent 实例配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功更新 Agent 实例"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:edit")
    public Response update(@PathParam("id") UUID id, @Valid AgentInstanceRecord.Update dto) {
        return Response.ok(agentInstanceService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除 Agent 实例")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功删除 Agent 实例"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:delete")
    public Response delete(@PathParam("id") UUID id) {
        agentInstanceService.delete(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/batch")
    @Operation(summary = "批量删除 Agent 实例")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "成功批量删除 Agent 实例"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:delete")
    public Response batchDelete(Map<String, List<UUID>> body) {
        List<UUID> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("ID 列表不能为空");
        }
        for (UUID id : ids) {
            agentInstanceService.delete(id);
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/commands")
    @Operation(summary = "向 Agent 实例发送命令")
    @APIResponses({
        @APIResponse(responseCode = "202", description = "命令已接受并执行"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response executeCommand(@PathParam("id") UUID id, @Valid AgentInstanceRecord.ExecuteCommand dto) {
        agentInstanceService.executeCommand(id, dto);
        return Response.accepted().build();
    }

    @POST
    @Path("/{id}/deploy")
    @Operation(summary = "在 Agent 实例上执行部署")
    @APIResponses({
        @APIResponse(responseCode = "202", description = "部署任务已接受"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:execute")
    public Response deploy(@PathParam("id") UUID id, @Valid AgentInstanceRecord.Deploy dto) {
        return Response.accepted()
                .entity(agentInstanceService.deploy(id, dto))
                .build();
    }

    @GET
    @Path("/{id}/tasks")
    @Operation(summary = "查询 Agent 实例任务历史")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务历史列表"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @Parameter(name = "status", description = "任务状态筛选（可选）", required = false)
    @Parameter(name = "startTime", description = "开始时间 ISO-8601 格式（可选）", required = false)
    @Parameter(name = "endTime", description = "结束时间 ISO-8601 格式（可选）", required = false)
    @Parameter(name = "page", description = "页码（默认 0）", required = false)
    @Parameter(name = "size", description = "每页大小（默认 20）", required = false)
    @RequiresPermission("agent:view")
    public Response getTaskHistory(
            @PathParam("id") UUID id,
            @QueryParam("status") AgentTaskStatus status,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        LocalDateTime startDateTime = startTime != null ? LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        LocalDateTime endDateTime = endTime != null ? LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        return Response.ok(agentInstanceService.queryTaskHistory(id, status, startDateTime, endDateTime, page, size)).build();
    }

    @GET
    @Path("/{id}/tasks/count")
    @Operation(summary = "统计 Agent 实例任务数量")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务数量"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @Parameter(name = "status", description = "任务状态筛选（可选）", required = false)
    @Parameter(name = "startTime", description = "开始时间 ISO-8601 格式（可选）", required = false)
    @Parameter(name = "endTime", description = "结束时间 ISO-8601 格式（可选）", required = false)
    @RequiresPermission("agent:view")
    public Response countTaskHistory(
            @PathParam("id") UUID id,
            @QueryParam("status") AgentTaskStatus status,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {
        LocalDateTime startDateTime = startTime != null ? LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        LocalDateTime endDateTime = endTime != null ? LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        return Response.ok(agentInstanceService.countTaskHistory(id, status, startDateTime, endDateTime)).build();
    }

    @GET
    @Path("/tasks/{taskId}")
    @Operation(summary = "获取任务详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务详情"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "taskId", description = "任务 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getTaskDetail(@PathParam("taskId") UUID taskId) {
        return Response.ok(agentInstanceService.getTaskDetail(taskId)).build();
    }

    @GET
    @Path("/{id}/status")
    @Operation(summary = "获取 Agent 实时运行状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回 Agent 运行状态"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getStatus(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.getRuntimeStatus(id)).build();
    }

    @GET
    @Path("/{id}/health")
    @Operation(summary = "获取 Agent 健康度信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回 Agent 健康度"),
        @APIResponse(responseCode = "404", description = "Agent 实例不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "Agent 实例 ID", required = true)
    @RequiresPermission("agent:view")
    public Response getHealth(@PathParam("id") UUID id) {
        return Response.ok(agentInstanceService.getHealth(id)).build();
    }
}
