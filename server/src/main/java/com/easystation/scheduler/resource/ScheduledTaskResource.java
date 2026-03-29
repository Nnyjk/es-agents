package com.easystation.scheduler.resource;

import com.easystation.scheduler.dto.ScheduledTaskRecord;
import com.easystation.scheduler.service.ScheduledTaskService;
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
import java.util.UUID;

@Path("/api/v1/scheduler/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "调度器任务管理", description = "系统级定时任务调度 API")
public class ScheduledTaskResource {

    @Inject
    ScheduledTaskService taskService;

    @GET
    @Operation(summary = "查询任务列表", description = "分页查询调度器任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "type", description = "按任务类型过滤", required = false)
    @Parameter(name = "status", description = "按任务状态过滤", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
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
    @Operation(summary = "获取任务详情", description = "获取指定调度器任务详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务详情"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(taskService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建任务", description = "创建新的调度器任务")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response create(@Valid ScheduledTaskRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(taskService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新任务", description = "更新现有调度器任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务更新成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    public Response update(@PathParam("id") UUID id, @Valid ScheduledTaskRecord.Update dto) {
        return Response.ok(taskService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除任务", description = "删除指定调度器任务")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "任务删除成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    public Response delete(@PathParam("id") UUID id) {
        taskService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/enable")
    @Operation(summary = "启用任务", description = "启用调度器任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务启用成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    public Response enable(@PathParam("id") UUID id) {
        return Response.ok(taskService.enable(id)).build();
    }

    @POST
    @Path("/{id}/disable")
    @Operation(summary = "禁用任务", description = "禁用调度器任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务禁用成功"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    public Response disable(@PathParam("id") UUID id) {
        return Response.ok(taskService.disable(id)).build();
    }

    @POST
    @Path("/{id}/execute")
    @Operation(summary = "立即执行任务", description = "立即触发调度器任务执行")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "任务执行已触发"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "id", description = "任务 ID", required = true)
    public Response executeNow(@PathParam("id") UUID id, @Valid ScheduledTaskRecord.ExecuteRequest dto) {
        return Response.ok(taskService.executeNow(id, dto)).build();
    }

    @GET
    @Path("/{id}/executions")
    @Operation(summary = "查询任务执行历史", description = "获取指定任务的执行历史")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行历史"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "taskId", description = "任务 ID", required = true)
    @Parameter(name = "status", description = "按执行状态过滤", required = false)
    @Parameter(name = "triggerType", description = "按触发类型过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
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
    @Operation(summary = "获取执行详情", description = "获取指定执行记录详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行详情"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "executionId", description = "执行 ID", required = true)
    public Response getExecution(@PathParam("executionId") UUID executionId) {
        return Response.ok(taskService.getExecution(executionId)).build();
    }

    @GET
    @Path("/executions")
    @Operation(summary = "查询所有执行记录", description = "分页查询所有执行记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行记录列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "taskId", description = "按任务 ID 过滤", required = false)
    @Parameter(name = "status", description = "按执行状态过滤", required = false)
    @Parameter(name = "triggerType", description = "按触发类型过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间 (ISO 8601)", required = false)
    @Parameter(name = "endTime", description = "结束时间 (ISO 8601)", required = false)
    @Parameter(name = "limit", description = "每页数量", required = false)
    @Parameter(name = "offset", description = "偏移量", required = false)
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
    @Operation(summary = "验证 Cron 表达式", description = "验证 Cron 表达式的有效性")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回验证结果"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "cron", description = "Cron 表达式", required = true)
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
    @Operation(summary = "获取统计信息", description = "获取调度器任务统计信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getStats() {
        return Response.ok(taskService.getStats()).build();
    }
}
