package com.easystation.pipeline.resource;

import com.easystation.auth.annotation.RequiresPermission;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/api/v1/pipelines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "流水线管理", description = "部署流水线定义与执行 API")
public class PipelineResource {

    @Inject
    PipelineService pipelineService;

    @GET
    @Operation(summary = "列出流水线", description = "分页列出部署流水线")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回流水线列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "status", description = "按状态过滤", required = false)
    @Parameter(name = "environmentId", description = "按环境 ID 过滤", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "limit", description = "每页数量（默认 20）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @RequiresPermission("pipeline:read")
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
    @Operation(summary = "获取流水线详情", description = "根据 ID 获取流水线详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回流水线详情"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "流水线 ID", required = true)
    @RequiresPermission("pipeline:read")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(pipelineService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建流水线", description = "创建新的部署流水线")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "流水线创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "dto", description = "流水线创建数据", required = true)
    @RequiresPermission("pipeline:write")
    public Response create(@Valid PipelineRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(pipelineService.create(dto))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新流水线", description = "更新现有流水线配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "流水线更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "流水线 ID", required = true)
    @Parameter(name = "dto", description = "流水线更新数据", required = true)
    @RequiresPermission("pipeline:write")
    public Response update(@PathParam("id") UUID id, @Valid PipelineRecord.Update dto) {
        return Response.ok(pipelineService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除流水线", description = "删除指定的部署流水线")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "流水线删除成功"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "流水线 ID", required = true)
    @RequiresPermission("pipeline:write")
    public Response delete(@PathParam("id") UUID id) {
        pipelineService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/execute")
    @Operation(summary = "执行流水线", description = "触发流水线执行")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "流水线执行触发成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "流水线 ID", required = true)
    @Parameter(name = "dto", description = "执行参数", required = true)
    @RequiresPermission("pipeline:execute")
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
    @Operation(summary = "列出执行记录", description = "获取流水线的执行历史记录")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行记录列表"),
        @APIResponse(responseCode = "404", description = "流水线不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "pipelineId", description = "流水线 ID", required = true)
    @Parameter(name = "status", description = "按执行状态过滤", required = false)
    @Parameter(name = "triggerType", description = "按触发类型过滤", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO-8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO-8601 格式）", required = false)
    @Parameter(name = "limit", description = "每页数量（默认 20）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @RequiresPermission("pipeline:read")
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
    @Operation(summary = "获取执行详情", description = "根据执行 ID 获取执行详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行详情"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "executionId", description = "执行记录 ID", required = true)
    @RequiresPermission("pipeline:read")
    public Response getExecution(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.getExecution(executionId)).build();
    }

    @POST
    @Path("/executions/{executionId}/advance")
    @Operation(summary = "推进执行阶段", description = "手动推进流水线执行到下一阶段")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "执行阶段推进成功"),
        @APIResponse(responseCode = "400", description = "无法推进（当前阶段未完成）"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "executionId", description = "执行记录 ID", required = true)
    @RequiresPermission("pipeline:execute")
    public Response advanceStage(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.advanceStage(executionId)).build();
    }

    @POST
    @Path("/executions/{executionId}/cancel")
    @Operation(summary = "取消执行", description = "取消正在进行的流水线执行")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "执行取消成功"),
        @APIResponse(responseCode = "400", description = "执行已完成或无法取消"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "executionId", description = "执行记录 ID", required = true)
    @RequiresPermission("pipeline:execute")
    public Response cancelExecution(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.cancelExecution(executionId)).build();
    }

    @GET
    @Path("/executions/{executionId}/stages")
    @Operation(summary = "获取执行阶段", description = "获取流水线执行的各阶段详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回执行阶段列表"),
        @APIResponse(responseCode = "404", description = "执行记录不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "executionId", description = "执行记录 ID", required = true)
    @RequiresPermission("pipeline:read")
    public Response getStages(@PathParam("executionId") UUID executionId) {
        return Response.ok(pipelineService.getStages(executionId)).build();
    }
}
