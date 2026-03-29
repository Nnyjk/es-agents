package com.easystation.deployment.resource;

import com.easystation.deployment.dto.*;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.deployment.enums.ReleaseStatus;
import com.easystation.deployment.service.ReleaseService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/deployment/releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "发布管理", description = "应用发布版本管理")
public class ReleaseResource {

    @Inject
    ReleaseService releaseService;

    @GET
    @Operation(summary = "获取发布列表", description = "分页查询发布版本列表，支持多条件筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回发布列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<ReleaseDTO> list(
            @Parameter(description = "页码") @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量") @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @Parameter(description = "发布 ID") @QueryParam("releaseId") String releaseId,
            @Parameter(description = "应用 ID") @QueryParam("applicationId") UUID applicationId,
            @Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId,
            @Parameter(description = "发布状态") @QueryParam("status") ReleaseStatus status) {
        return releaseService.listReleases(pageNum, pageSize, releaseId, applicationId, environmentId, status);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取发布详情", description = "根据 ID 查询发布版本基本信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回发布详情"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ReleaseDTO get(@Parameter(description = "发布 ID") @PathParam("id") UUID id) {
        return releaseService.getRelease(id);
    }

    @GET
    @Path("/{id}/detail")
    @Operation(summary = "获取发布完整详情", description = "查询发布版本的完整详细信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回发布完整详情"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ReleaseDTO getDetail(@Parameter(description = "发布 ID") @PathParam("id") UUID id) {
        return releaseService.getReleaseDetail(id);
    }

    @POST
    @Operation(summary = "创建发布", description = "创建新的应用发布版本")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功创建发布"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "发布版本已存在")
    })
    @RequiresPermission("deployment:create")
    public ReleaseDTO create(ReleaseDTO dto) {
        return releaseService.createRelease(dto);
    }

    @POST
    @Path("/{id}/submit")
    @Operation(summary = "提交审批", description = "提交发布版本进行审批")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功提交审批"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public ReleaseDTO submitForApproval(@Parameter(description = "发布 ID") @PathParam("id") UUID id,
                                        @Parameter(description = "提交人") @QueryParam("submittedBy") @DefaultValue("system") String submittedBy) {
        return releaseService.submitForApproval(id, submittedBy);
    }

    @POST
    @Path("/{id}/approve")
    @Operation(summary = "批准发布", description = "批准发布版本进行部署")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功批准发布"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public ReleaseDTO approve(@Parameter(description = "发布 ID") @PathParam("id") UUID id,
                              @Parameter(description = "批准人") @QueryParam("approvedBy") @DefaultValue("system") String approvedBy) {
        return releaseService.approveRelease(id, approvedBy);
    }

    @POST
    @Path("/{id}/reject")
    @Operation(summary = "拒绝发布", description = "拒绝发布版本并说明原因")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功拒绝发布"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public ReleaseDTO reject(@Parameter(description = "发布 ID") @PathParam("id") UUID id,
                             @Parameter(description = "拒绝人") @QueryParam("rejectedBy") @DefaultValue("system") String rejectedBy,
                             @Parameter(description = "拒绝原因") @QueryParam("reason") String reason) {
        return releaseService.rejectRelease(id, rejectedBy, reason);
    }

    @POST
    @Path("/{id}/start")
    @Operation(summary = "开始部署", description = "启动发布版本的部署流程")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功开始部署"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public ReleaseDTO start(@Parameter(description = "发布 ID") @PathParam("id") UUID id,
                            @Parameter(description = "部署人") @QueryParam("deployedBy") @DefaultValue("system") String deployedBy) {
        return releaseService.startRelease(id, deployedBy);
    }

    @POST
    @Path("/{id}/rollback")
    @Operation(summary = "回滚发布", description = "回滚到之前的发布版本")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功回滚发布"),
        @APIResponse(responseCode = "404", description = "发布不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:create")
    public ReleaseDTO rollback(@Parameter(description = "发布 ID") @PathParam("id") UUID id,
                               @Parameter(description = "回滚人") @QueryParam("rolledBackBy") @DefaultValue("system") String rolledBackBy) {
        return releaseService.rollbackRelease(id, rolledBackBy);
    }

    @GET
    @Path("/history/{applicationId}")
    @Operation(summary = "获取发布历史", description = "查询应用的发布历史记录")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回发布历史"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public List<ReleaseDTO> getHistory(@Parameter(description = "应用 ID") @PathParam("applicationId") UUID applicationId) {
        return releaseService.getReleaseHistory(applicationId);
    }
}