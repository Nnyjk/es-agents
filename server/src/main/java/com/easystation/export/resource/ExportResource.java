package com.easystation.export.resource;

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

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

@Path("/api/v1/export")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExportResource {

    @Inject
    ExportService exportService;

    /**
     * Create a new export task
     */
    @POST
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

    /**
     * Get export task status
     */
    @GET
    @Path("/{taskId}")
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

    /**
     * Download export file
     */
    @GET
    @Path("/{taskId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
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

    /**
     * Get user's export task list
     */
    @GET
    @Path("/tasks")
    public Response getUserTasks(@Context SecurityContext securityContext) {
        UUID userId = getCurrentUserId(securityContext);

        ExportTaskListResponse response = exportService.getUserTasks(userId);
        return Response.ok(response).build();
    }

    /**
     * Get current user ID from security context
     */
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