package com.easystation.export.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.export.dto.ExportRequest;
import com.easystation.export.dto.ExportResponse;
import com.easystation.export.dto.ExportTaskDTO;
import com.easystation.export.dto.ExportTaskListResponse;
import com.easystation.export.service.ExportService;
import jakarta.inject.Inject;
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

import java.io.FileNotFoundException;
import java.util.UUID;

@Path("/api/v1/export")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "数据导出", description = "数据导出任务管理 API")
public class ExportResource {

    @Inject
    ExportService exportService;

    @POST
    @Operation(summary = "创建导出任务", description = "创建新的数据导出任务（支持 Excel/PDF 格式）")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "导出任务创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "exportType", description = "导出格式（EXCEL/PDF）", required = true)
    @Parameter(name = "dataType", description = "数据类型（DEPLOYMENT_HISTORY/COMMAND_LOG/AUDIT_LOG/ALERT）", required = true)
    @RequiresPermission("export:create")
    public Response createExport(
            @Context SecurityContext securityContext,
            ExportRequest request) {

        UUID userId = getCurrentUserId(securityContext);

        // Validate request
        if (request.exportType() == null || request.dataType() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ExportResponse(null, "ERROR", "exportType and dataType are required"))
                .build();
        }

        // Validate export type
        if (!"EXCEL".equals(request.exportType()) && !"PDF".equals(request.exportType())) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ExportResponse(null, "ERROR", "Invalid exportType. Must be EXCEL or PDF"))
                .build();
        }

        // Validate data type
        String[] validDataTypes = {"DEPLOYMENT_HISTORY", "COMMAND_LOG", "AUDIT_LOG", "ALERT"};
        boolean validDataType = false;
        for (String dt : validDataTypes) {
            if (dt.equals(request.dataType())) {
                validDataType = true;
                break;
            }
        }
        if (!validDataType) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ExportResponse(null, "ERROR", "Invalid dataType. Must be one of: DEPLOYMENT_HISTORY, COMMAND_LOG, AUDIT_LOG, ALERT"))
                .build();
        }

        UUID taskId = exportService.createExportTask(userId, request);
        return Response.ok(new ExportResponse(taskId, "PENDING", "Export task created successfully"))
            .build();
    }

    @GET
    @Path("/{taskId}")
    @Operation(summary = "获取任务状态", description = "查询导出任务的当前状态")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务状态"),
        @APIResponse(responseCode = "404", description = "任务不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "taskId", description = "导出任务 ID", required = true)
    @RequiresPermission("export:read")
    public Response getTaskStatus(
            @Context SecurityContext securityContext,
            @PathParam("taskId") UUID taskId) {

        UUID userId = getCurrentUserId(securityContext);

        ExportTaskDTO task = exportService.getTaskStatus(taskId);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ExportResponse(null, "ERROR", "Export task not found"))
                .build();
        }

        return Response.ok(task).build();
    }

    @GET
    @Path("/{taskId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "下载导出文件", description = "下载已完成的导出文件")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回文件内容"),
        @APIResponse(responseCode = "400", description = "任务未完成"),
        @APIResponse(responseCode = "404", description = "任务或文件不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "taskId", description = "导出任务 ID", required = true)
    @RequiresPermission("export:read")
    public Response downloadFile(
            @Context SecurityContext securityContext,
            @PathParam("taskId") UUID taskId) {

        UUID userId = getCurrentUserId(securityContext);

        ExportTaskDTO task = exportService.getTaskStatus(taskId);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ExportResponse(null, "ERROR", "Export task not found"))
                .build();
        }

        if (!"COMPLETED".equals(task.status())) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ExportResponse(null, "ERROR", "Export task is not completed"))
                .build();
        }

        try {
            byte[] fileContent = exportService.getExportFile(taskId);
            String fileName = task.fileName() != null ? task.fileName() : "export.xlsx";

            return Response.ok(fileContent)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
        } catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ExportResponse(null, "ERROR", "Export file not found"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ExportResponse(null, "ERROR", "Failed to download file: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/tasks")
    @Operation(summary = "获取任务列表", description = "获取当前用户的所有导出任务列表")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回任务列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @RequiresPermission("export:read")
    public Response getUserTasks(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);

        ExportTaskListResponse response = exportService.getUserTasks(userId);
        return Response.ok(response).build();
    }

    private UUID getCurrentUserId(SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            // For testing purposes, use a mock user ID
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        try {
            return UUID.fromString(securityContext.getUserPrincipal().getName());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid user identity", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
