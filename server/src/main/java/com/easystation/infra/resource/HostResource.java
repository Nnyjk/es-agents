package com.easystation.infra.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.infra.record.HostRecord;
import com.easystation.infra.service.HostService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.UUID;

@Path("/api/v1/hosts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "主机管理", description = "主机管理 API")
public class HostResource {

    @Inject
    HostService hostService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "获取主机列表", description = "查询所有主机，支持按环境 ID 筛选")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回主机列表"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:view")
    public Response list(@Parameter(description = "环境 ID") @QueryParam("environmentId") UUID environmentId) {
        return Response.ok(hostService.list(environmentId)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取主机详情", description = "根据 ID 查询单个主机详情")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回主机详情"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:view")
    public Response get(@Parameter(description = "主机 ID") @PathParam("id") UUID id) {
        return Response.ok(hostService.get(id)).build();
    }

    @POST
    @Operation(summary = "创建主机", description = "创建新的主机记录")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "成功创建主机"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "主机名称已存在")
    })
    @RequiresPermission("host:create")
    public Response create(@Valid HostRecord.Create dto) {
        HostRecord created = hostService.create(dto);
        recordAuditLog(AuditAction.CREATE_HOST, AuditResult.SUCCESS,
                "创建主机：" + created.name(), "Host", created.id());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新主机", description = "更新指定主机的配置信息")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功更新主机"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "主机名称已存在")
    })
    @RequiresPermission("host:edit")
    public Response update(@Parameter(description = "主机 ID") @PathParam("id") UUID id, @Valid HostRecord.Update dto) {
        HostRecord updated = hostService.update(id, dto);
        recordAuditLog(AuditAction.UPDATE_HOST, AuditResult.SUCCESS,
                "更新主机：" + updated.name(), "Host", id);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除主机", description = "删除指定的主机")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "成功删除主机"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足"),
        @APIResponse(responseCode = "409", description = "主机仍有关联资源，无法删除")
    })
    @RequiresPermission("host:delete")
    public Response delete(@Parameter(description = "主机 ID") @PathParam("id") UUID id) {
        HostRecord host = hostService.get(id);
        hostService.delete(id);
        recordAuditLog(AuditAction.DELETE_HOST, AuditResult.SUCCESS,
                "删除主机：" + (host != null ? host.name() : id), "Host", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/connect")
    @Operation(summary = "连接主机", description = "建立与指定主机的连接")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功连接主机"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:manage")
    public Response connect(@Parameter(description = "主机 ID") @PathParam("id") UUID id) {
        hostService.connect(id);
        recordAuditLog(AuditAction.START_AGENT, AuditResult.SUCCESS,
                "连接主机", "Host", id);
        return Response.ok().build();
    }

    /**
     * Check host reachability - verifies if the host is accessible via TCP
     */
    @POST
    @Path("/{id}/check-reachability")
    @Operation(summary = "检查主机可达性", description = "验证指定主机是否可通过 TCP 访问")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回可达性状态"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:view")
    public Response checkReachability(@Parameter(description = "主机 ID") @PathParam("id") UUID id) {
        return Response.ok(hostService.checkReachability(id)).build();
    }

    /**
     * Check reachability of all hosts
     */
    @POST
    @Path("/check-reachability")
    @Operation(summary = "检查所有主机可达性", description = "批量验证所有主机的 TCP 连接状态")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回所有主机可达性状态"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:view")
    public Response checkReachabilityAll() {
        return Response.ok(hostService.checkReachabilityAll()).build();
    }

    @GET
    @Path("/{id}/install-guide")
    @Operation(summary = "获取安装指南", description = "获取指定主机的 Agent 安装指南")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回安装指南"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:view")
    public Response getInstallGuide(@Parameter(description = "主机 ID") @PathParam("id") UUID id) {
        return Response.ok(hostService.getInstallGuide(id)).build();
    }

    @GET
    @Path("/{id}/package")
    @Operation(summary = "下载主机安装包", description = "下载指定主机的 Agent 安装包")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "成功返回安装包文件"),
        @APIResponse(responseCode = "400", description = "缺少 sourceId 参数"),
        @APIResponse(responseCode = "404", description = "主机不存在"),
        @APIResponse(responseCode = "401", description = "未授权"),
        @APIResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("host:view")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPackage(@Parameter(description = "主机 ID") @PathParam("id") UUID id, @Parameter(description = "制品源 ID") @QueryParam("sourceId") UUID sourceId) {
        if (sourceId == null) {
            throw new WebApplicationException("sourceId is required", Response.Status.BAD_REQUEST);
        }

        StreamingOutput stream = hostService.downloadPackage(id, sourceId);
        String packageFileName = hostService.getPackageFileName(id);

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"" + packageFileName + "\"")
                .build();
    }

    private void recordAuditLog(AuditAction action, AuditResult result,
                               String description, String resourceType, UUID resourceId) {
        try {
            String username = securityContext != null && securityContext.getUserPrincipal() != null
                    ? securityContext.getUserPrincipal().getName() : "system";
            String clientIp = httpHeaders != null
                    ? httpHeaders.getHeaderString("X-Forwarded-For")
                    : null;
            if (clientIp == null && httpHeaders != null) {
                clientIp = httpHeaders.getHeaderString("X-Real-IP");
            }
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/v1/hosts");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}
