package com.easystation.agent.resource;

import com.easystation.agent.domain.enums.ScheduledTaskCategory;
import com.easystation.agent.record.ScheduledTaskExecutionRecord;
import com.easystation.agent.record.ScheduledTaskRecord;
import com.easystation.agent.service.ScheduledTaskService;
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
import java.util.UUID;

@Path("/api/v1/scheduled-tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "定时任务管理", description = "定时任务配置、调度与执行 API")
public class ScheduledTaskResource {

    @Inject
    ScheduledTaskService scheduledTaskService;

    @GET
    @Operation(summary = "查询任务列表", description = "查询所有定时任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "category", description = "按分类过滤", required = false)
    @Parameter(name = "activeOnly", description = "仅查询启用状态", required = false)
    @RequiresPermission("agent:view")
    public List<ScheduledTaskRecord.ListResponse> list(
            @QueryParam("category") ScheduledTaskCategory category,
            @QueryParam("activeOnly") Boolean activeOnly
    ) {
        return scheduledTaskService.list(category, activeOnly);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取任务详情", description = "获取指定定时任务详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务详情"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RequiresPermission("agent:view")
    public ScheduledTaskRecord.DetailResponse getById(@PathParam("id") UUID id) {
        return scheduledTaskService.getById(id);
    }

    @POST
    @Operation(summary = "创建任务", description = "创建新的定时任务")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:create")
    public Response create(@Valid ScheduledTaskRecord.CreateRequest request) {
        ScheduledTaskRecord.DetailResponse task = scheduledTaskService.create(request);
        return Response.status(Response.Status.CREATED).entity(task).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新任务", description = "更新现有定时任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务更新成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RequiresPermission("agent:edit")
    public ScheduledTaskRecord.DetailResponse update(
            @PathParam("id") UUID id,
            @Valid ScheduledTaskRecord.UpdateRequest request
    ) {
        return scheduledTaskService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除任务", description = "删除指定定时任务")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "任务删除成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RequiresPermission("agent:delete")
    public void delete(@PathParam("id") UUID id) {
        scheduledTaskService.delete(id);
    }

    @POST
    @Path("/{id}/enable")
    @Operation(summary = "启用任务", description = "启用定时任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务启用成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RequiresPermission("agent:execute")
    public ScheduledTaskRecord.DetailResponse enable(@PathParam("id") UUID id) {
        return scheduledTaskService.enable(id);
    }

    @POST
    @Path("/{id}/disable")
    @Operation(summary = "禁用任务", description = "禁用定时任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务禁用成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RequiresPermission("agent:execute")
    public ScheduledTaskRecord.DetailResponse disable(@PathParam("id") UUID id) {
        return scheduledTaskService.disable(id);
    }

    @POST
    @Path("/{id}/execute")
    @Operation(summary = "立即执行任务", description = "立即触发定时任务执行")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务执行已触发"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @Parameter(name = "agentInstanceId", description = "Agent 实例 ID", required = false)
    @RequiresPermission("agent:execute")
    public ScheduledTaskExecutionRecord.DetailResponse executeNow(
            @PathParam("id") UUID id,
            @Context SecurityContext securityContext,
            @QueryParam("agentInstanceId") UUID agentInstanceId
    ) {
        if (agentInstanceId == null) {
            agentInstanceId = UUID.randomUUID();
        }
        return scheduledTaskService.executeNow(id, agentInstanceId);
    }

    @GET
    @Path("/{id}/executions")
    @Operation(summary = "查询执行历史", description = "获取任务的执行历史记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行历史"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    @RequiresPermission("agent:view")
    public List<ScheduledTaskExecutionRecord.ListResponse> getExecutionHistory(@PathParam("id") UUID id) {
        return scheduledTaskService.getExecutionHistory(id);
    }

    @GET
    @Path("/executions/{executionId}")
    @Operation(summary = "获取执行详情", description = "获取指定执行记录详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行详情"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @RequiresPermission("agent:view")
    public ScheduledTaskExecutionRecord.DetailResponse getExecutionById(@PathParam("executionId") UUID executionId) {
        return scheduledTaskService.getExecutionById(executionId);
    }

    @PUT
    @Path("/executions/{executionId}/status")
    @Operation(summary = "更新执行状态", description = "更新执行记录的状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "状态更新成功"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    @RequiresPermission("agent:edit")
    public ScheduledTaskExecutionRecord.DetailResponse updateExecutionStatus(
            @PathParam("executionId") UUID executionId,
            @Valid ScheduledTaskExecutionRecord.UpdateStatusRequest request
    ) {
        return scheduledTaskService.updateExecutionStatus(executionId, request);
    }

    @POST
    @Path("/validate-cron")
    @Operation(summary = "验证 Cron 表达式", description = "验证 Cron 表达式的有效性")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回验证结果"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:view")
    public ScheduledTaskRecord.CronValidateResponse validateCron(
            @Valid ScheduledTaskRecord.CronValidateRequest request
    ) {
        boolean isValid = scheduledTaskService.isValidCronExpression(request.cronExpression());
        String message = isValid ? "Valid cron expression" : "Invalid cron expression";
        return new ScheduledTaskRecord.CronValidateResponse(isValid, message, null);
    }

    @GET
    @Path("/due")
    @Operation(summary = "查询待执行任务", description = "查询当前需要执行的任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回待执行任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("agent:view")
    public List<ScheduledTaskRecord.ListResponse> findDueTasks() {
        return scheduledTaskService.findDueTasks();
    }
}
