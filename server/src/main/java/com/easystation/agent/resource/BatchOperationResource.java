package com.easystation.agent.resource;

import com.easystation.agent.domain.BatchOperation;
import com.easystation.agent.domain.BatchOperationItem;
import com.easystation.agent.dto.BatchCommandRequest;
import com.easystation.agent.dto.BatchDeployRequest;
import com.easystation.agent.dto.BatchOperationItemResponse;
import com.easystation.agent.dto.BatchOperationResponse;
import com.easystation.agent.dto.BatchUpgradeRequest;
import com.easystation.agent.service.BatchOperationService;
import com.easystation.auth.annotation.RequiresPermission;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/batch-operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Batch Operations", description = "APIs for batch operations on agents and hosts")
public class BatchOperationResource {

    @Inject
    BatchOperationService batchOperationService;

    /**
     * Execute batch command on multiple hosts.
     */
    @POST
    @Path("/commands")
    @RequiresPermission("batch:operate")
    @Operation(summary = "Execute batch command", description = "Execute a command on multiple hosts")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Batch command operation created"),
        @APIResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public Response executeBatchCommand(
            @Valid BatchCommandRequest request,
            @Context SecurityContext securityContext) {
        UUID operatorId = getUserId(securityContext);
        BatchOperation operation = batchOperationService.createBatchCommand(
                request.hostIds(),
                request.command(),
                operatorId
        );
        return Response.status(Response.Status.CREATED)
                .entity(toResponse(operation))
                .build();
    }

    /**
     * Batch deploy agents.
     */
    @POST
    @Path("/deploy")
    @RequiresPermission("batch:operate")
    @Operation(summary = "Batch deploy", description = "Deploy multiple agent instances")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Batch deploy operation created"),
        @APIResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public Response batchDeploy(
            @Valid BatchDeployRequest request,
            @Context SecurityContext securityContext) {
        UUID operatorId = getUserId(securityContext);
        BatchOperation operation = batchOperationService.createBatchDeploy(
                request.agentIds(),
                operatorId
        );
        return Response.status(Response.Status.CREATED)
                .entity(toResponse(operation))
                .build();
    }

    /**
     * Batch upgrade agents.
     */
    @POST
    @Path("/upgrade")
    @RequiresPermission("batch:operate")
    @Operation(summary = "Batch upgrade", description = "Upgrade multiple agent instances to a new version")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Batch upgrade operation created"),
        @APIResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public Response batchUpgrade(
            @Valid BatchUpgradeRequest request,
            @Context SecurityContext securityContext) {
        UUID operatorId = getUserId(securityContext);
        BatchOperation operation = batchOperationService.createBatchUpgrade(
                request.agentIds(),
                request.version(),
                operatorId
        );
        return Response.status(Response.Status.CREATED)
                .entity(toResponse(operation))
                .build();
    }

    /**
     * Get batch operation details by ID.
     */
    @GET
    @Path("/{id}")
    @RequiresPermission("batch:operate")
    @Operation(summary = "Get operation details", description = "Get details of a batch operation by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Batch operation details"),
        @APIResponse(responseCode = "404", description = "Batch operation not found")
    })
    public Response getOperation(@PathParam("id") UUID id) {
        BatchOperation operation = batchOperationService.getBatchOperation(id);
        return Response.ok(toResponse(operation)).build();
    }

    /**
     * Get batch operation items.
     */
    @GET
    @Path("/{id}/items")
    @RequiresPermission("batch:operate")
    @Operation(summary = "Get operation items", description = "Get all items of a batch operation")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of batch operation items"),
        @APIResponse(responseCode = "404", description = "Batch operation not found")
    })
    public Response getOperationItems(@PathParam("id") UUID id) {
        // First verify the operation exists
        batchOperationService.getBatchOperation(id);

        List<BatchOperationItem> items = batchOperationService.getBatchOperationItems(id);
        List<BatchOperationItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        return Response.ok(itemResponses).build();
    }

    /**
     * List batch operations with pagination.
     */
    @GET
    @RequiresPermission("batch:operate")
    @Operation(summary = "List operations", description = "List batch operations history with pagination")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of batch operations")
    })
    public Response listOperations(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        List<BatchOperation> operations = batchOperationService.listBatchOperations(page, size);
        long total = batchOperationService.countBatchOperations();

        List<BatchOperationResponse> operationResponses = operations.stream()
                .map(this::toResponse)
                .toList();

        return Response.ok()
                .entity(Map.of(
                        "data", operationResponses,
                        "total", total,
                        "page", page,
                        "size", size
                ))
                .build();
    }

    /**
     * Convert BatchOperation entity to response DTO.
     */
    private BatchOperationResponse toResponse(BatchOperation operation) {
        return new BatchOperationResponse(
                operation.id,
                operation.operationType,
                operation.status,
                operation.operatorId,
                operation.createdAt,
                operation.completedAt,
                operation.totalItems,
                operation.successCount,
                operation.failedCount
        );
    }

    /**
     * Convert BatchOperationItem entity to response DTO.
     */
    private BatchOperationItemResponse toItemResponse(BatchOperationItem item) {
        return new BatchOperationItemResponse(
                item.id,
                item.targetId,
                item.targetType,
                item.status,
                item.errorMessage,
                item.startedAt,
                item.completedAt,
                item.createdAt
        );
    }

    /**
     * Get user ID from security context.
     */
    private UUID getUserId(SecurityContext securityContext) {
        // Default to a system UUID if no user context is available
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        String username = securityContext.getUserPrincipal().getName();
        // In real implementation, this should lookup the actual user ID
        // For now, we use a placeholder conversion
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            // If username is not a valid UUID, use system UUID
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }
}