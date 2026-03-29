package com.easystation.agent.planning.resource;

import com.easystation.agent.planning.domain.enums.PlanningTaskStatus;
import com.easystation.agent.planning.dto.*;
import com.easystation.agent.planning.service.TaskService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.annotation.security.RolesAllowed;
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

import java.util.List;
import java.util.UUID;

/**
 * 任务规划 REST API
 * 提供任务分解、调度、执行和管理的接口
 */
@Path("/api/agent/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agent 任务规划", description = "任务分解、调度、执行和管理 API")
public class TaskResource {

    @Inject
    TaskService taskService;

    /**
     * 分解目标为任务
     */
    @POST
    @Path("/decompose")
    @Operation(summary = "分解目标为任务", description = "将目标描述分解为可执行的任务列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务分解成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public DecomposeResponse decompose(@Valid DecomposeRequest request) {
        return taskService.decompose(request);
    }

    /**
     * 获取任务详情
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "获取任务详情", description = "根据 ID 获取任务的详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务详情"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public TaskRecord getById(@PathParam("id") UUID id) {
        return taskService.getById(id);
    }

    /**
     * 更新任务状态
     */
    @PUT
    @Path("/{id}/status")
    @Operation(summary = "更新任务状态", description = "手动更新任务的状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务状态更新成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public TaskRecord updateStatus(
            @PathParam("id") UUID id,
            @Valid TaskRecord.UpdateStatusRequest request) {
        return taskService.updateStatus(id, request);
    }

    /**
     * 执行任务
     */
    @POST
    @Path("/{id}/execute")
    @Operation(summary = "执行任务", description = "执行指定 ID 的任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务执行完成"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "400", description = "任务无法执行"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public ExecutionResultResponse execute(@PathParam("id") UUID id) {
        return taskService.execute(id);
    }

    /**
     * 取消任务
     */
    @POST
    @Path("/{id}/cancel")
    @Operation(summary = "取消任务", description = "取消正在执行或等待执行的任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务取消成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "400", description = "任务无法取消"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public TaskRecord cancel(@PathParam("id") UUID id) {
        return taskService.cancel(id);
    }

    /**
     * 查询任务列表
     */
    @GET
    @Operation(summary = "查询任务列表", description = "查询所有任务，支持按状态筛选")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "status", description = "任务状态筛选（可选）", required = false)
    @Parameter(name = "limit", description = "返回数量限制（默认 50）", required = false)
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public TaskListResponse list(
            @QueryParam("status") PlanningTaskStatus status,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        return taskService.list(status, limit);
    }

    /**
     * 获取目标下的所有任务
     */
    @GET
    @Path("/goals/{goalId}")
    @Operation(summary = "获取目标下的所有任务", description = "获取指定目标 ID 下的所有任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务列表"),
        @APIResponse(responseCode = "404", description = "目标不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "goalId", description = "目标 ID", required = true)
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public TaskListResponse listByGoal(@PathParam("goalId") UUID goalId) {
        return taskService.listByGoal(goalId);
    }

    /**
     * 创建单个任务
     */
    @POST
    @Operation(summary = "创建单个任务", description = "手动创建一个任务")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public Response create(@Valid TaskRecord.Create request) {
        TaskRecord task = taskService.create(request);
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    /**
     * 调度目标下的任务
     */
    @POST
    @Path("/goals/{goalId}/schedule")
    @Operation(summary = "调度目标下的任务", description = "将目标下 READY 状态的任务转换为 SCHEDULED 状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务调度成功"),
        @APIResponse(responseCode = "404", description = "目标不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "goalId", description = "目标 ID", required = true)
    @RolesAllowed({"Admin", "Ops"})
    @RequiresPermission("agent:execute")
    public List<TaskRecord> scheduleGoal(@PathParam("goalId") UUID goalId) {
        return taskService.scheduleGoal(goalId);
    }

    /**
     * 获取任务状态统计
     */
    @GET
    @Path("/counts")
    @Operation(summary = "获取任务状态统计", description = "获取各状态的任务数量统计")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RolesAllowed({"Admin", "Ops", "Viewer"})
    @RequiresPermission("agent:view")
    public TaskRecord.TaskCounts getCounts() {
        return taskService.getCounts();
    }
}