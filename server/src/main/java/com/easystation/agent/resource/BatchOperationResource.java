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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/batch-operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "批量操作管理", description = "批量命令执行、部署、升级 API")
public class BatchOperationResource {

    @Inject
    BatchOperationService batchOperationService;

    @POST
    @Path("/commands")
    @Operation(summary = "执行批量命令", description = "在多个主机上执行命令")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "批量命令操作创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "request", description = "批量命令请求", required = true)
    @RequiresPermission("batch:operate")
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

    @POST
    @Path("/deploy")
    @Operation(summary = "批量部署", description = "部署多个 Agent 实例")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "批量部署操作创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "request", description = "批量部署请求", required = true)
    @RequiresPermission("batch:operate")
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

    @POST
    @Path("/upgrade")
    @Operation(summary = "批量升级", description = "将多个 Agent 实例升级到新版本")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "批量升级操作创建成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "request", description = "批量升级请求", required = true)
    @RequiresPermission("batch:operate")
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

    @GET
    @Path("/{id}")
    @Operation(summary = "获取操作详情", description = "根据 ID 获取批量操作详情")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回批量操作详情"),
        @APIResponse(responseCode = "404", description = "批量操作不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "批量操作 ID", required = true)
    @RequiresPermission("batch:operate")
    public Response getOperation(@PathParam("id") UUID id) {
        BatchOperation operation = batchOperationService.getBatchOperation(id);
        return Response.ok(toResponse(operation)).build();
    }

    @GET
    @Path("/{id}/items")
    @Operation(summary = "获取操作项列表", description = "获取批量操作的所有子项")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回操作项列表"),
        @APIResponse(responseCode = "404", description = "批量操作不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "id", description = "批量操作 ID", required = true)
    @RequiresPermission("batch:operate")
    public Response getOperationItems(@PathParam("id") UUID id) {
        // First verify the operation exists
        batchOperationService.getBatchOperation(id);

        List<BatchOperationItem> items = batchOperationService.getBatchOperationItems(id);
        List<BatchOperationItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        return Response.ok(itemResponses).build();
    }

    @GET
    @Operation(summary = "列出操作历史", description = "分页列出批量操作历史")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回操作列表"),
        @APIResponse(responseCode = "401", description = "未授权访问"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @Parameter(name = "page", description = "页码（从 0 开始，默认 0）", required = false)
    @Parameter(name = "size", description = "每页数量（默认 20）", required = false)
    @RequiresPermission("batch:operate")
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
