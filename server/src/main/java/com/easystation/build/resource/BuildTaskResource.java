package com.easystation.build.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.build.dto.BuildRecord;
import com.easystation.build.enums.BuildStatus;
import com.easystation.build.enums.BuildType;
import com.easystation.build.service.BuildTaskService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/builds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "构建任务管理", description = "构建任务执行与制品管理 API")
public class BuildTaskResource {

    @Inject
    BuildTaskService buildTaskService;

    @GET
    @Operation(summary = "列出构建任务", description = "分页列出构建任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回构建任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "type", description = "按构建类型过滤", required = false)
    @Parameter(name = "status", description = "按构建状态过滤", required = false)
    @Parameter(name = "templateId", description = "按模板 ID 过滤", required = false)
    @Parameter(name = "keyword", description = "关键词搜索", required = false)
    @Parameter(name = "startTime", description = "开始时间（ISO-8601 格式）", required = false)
    @Parameter(name = "endTime", description = "结束时间（ISO-8601 格式）", required = false)
    @Parameter(name = "limit", description = "每页数量（默认 20）", required = false)
    @Parameter(name = "offset", description = "偏移量（默认 0）", required = false)
    @RequiresPermission("build:read")
    public Response list(
            @QueryParam("type") BuildType type,
            @QueryParam("status") BuildStatus status,
            @QueryParam("templateId") UUID templateId,
            @QueryParam("keyword") String keyword,
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {

        LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
        LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;

        BuildRecord.Query query = new BuildRecord.Query(
                type, status, templateId, keyword, startTime, endTime, limit, offset
        );
        return Response.ok(buildTaskService.list(query)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取构建任务详情", description = "根据 ID 获取构建任务详细信息")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回构建任务详情"),
        @APIResponse(responseCode = "404", description = "构建任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "构建任务 ID", required = true)
    @RequiresPermission("build:read")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(buildTaskService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建构建任务", description = "创建新的构建任务")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "构建任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "dto", description = "构建任务创建数据", required = true)
    @RequiresPermission("build:write")
    public Response create(@Valid BuildRecord.Create dto) {
        return Response.status(Response.Status.CREATED)
                .entity(buildTaskService.create(dto))
                .build();
    }

    @POST
    @Path("/{id}/start")
    @Operation(summary = "启动构建", description = "启动等待中的构建任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "构建启动成功"),
        @APIResponse(responseCode = "400", description = "构建无法启动（状态不允许）"),
        @APIResponse(responseCode = "404", description = "构建任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "构建任务 ID", required = true)
    @RequiresPermission("build:execute")
    public Response start(@PathParam("id") UUID id) {
        return Response.ok(buildTaskService.start(id)).build();
    }

    @POST
    @Path("/{id}/cancel")
    @Operation(summary = "取消构建", description = "取消正在进行的构建任务")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "构建取消成功"),
        @APIResponse(responseCode = "400", description = "构建无法取消（已完成）"),
        @APIResponse(responseCode = "404", description = "构建任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "构建任务 ID", required = true)
    @RequiresPermission("build:execute")
    public Response cancel(@PathParam("id") UUID id) {
        return Response.ok(buildTaskService.cancel(id)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除构建任务", description = "删除指定的构建任务")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "构建任务删除成功"),
        @APIResponse(responseCode = "404", description = "构建任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "构建任务 ID", required = true)
    @RequiresPermission("build:write")
    public Response delete(@PathParam("id") UUID id) {
        buildTaskService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/artifacts")
    @Operation(summary = "列出构建制品", description = "获取构建任务产生的所有制品")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回制品列表"),
        @APIResponse(responseCode = "404", description = "构建任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "taskId", description = "构建任务 ID", required = true)
    @RequiresPermission("build:read")
    public Response listArtifacts(@PathParam("id") UUID taskId) {
        return Response.ok(buildTaskService.listArtifacts(taskId)).build();
    }

    @POST
    @Path("/{id}/artifacts")
    @Operation(summary = "创建构建制品", description = "为构建任务创建新的制品记录")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "制品创建成功"),
        @APIResponse(responseCode = "404", description = "构建任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "taskId", description = "构建任务 ID", required = true)
    @RequiresPermission("build:write")
    public Response createArtifact(@PathParam("id") UUID taskId) {
        return Response.status(Response.Status.CREATED)
                .entity(buildTaskService.createArtifact(taskId))
                .build();
    }

    @GET
    @Path("/counts")
    @Operation(summary = "获取构建统计", description = "获取各状态构建任务的数量统计")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回统计信息"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("build:read")
    public Response counts() {
        return Response.ok(Map.of(
                "pending", buildTaskService.countByStatus(BuildStatus.PENDING),
                "running", buildTaskService.countByStatus(BuildStatus.RUNNING),
                "success", buildTaskService.countByStatus(BuildStatus.SUCCESS),
                "failed", buildTaskService.countByStatus(BuildStatus.FAILED),
                "cancelled", buildTaskService.countByStatus(BuildStatus.CANCELLED)
        )).build();
    }

    @GET
    @Path("/types")
    @Operation(summary = "获取构建类型列表", description = "获取所有可用的构建类型定义")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回构建类型列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("build:read")
    public Response getBuildTypes() {
        List<TypeInfo> types = List.of(BuildType.values()).stream()
                .map(t -> new TypeInfo(t.name(), t.name().replace("_", " ").toLowerCase()))
                .toList();
        return Response.ok(types).build();
    }

    record TypeInfo(String name, String description) {}
}
