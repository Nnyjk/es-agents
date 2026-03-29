package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ArtifactRepository;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.audit.enums.AuditAction;
import com.easystation.audit.enums.AuditResult;
import com.easystation.audit.service.AuditLogService;
import com.easystation.deployment.dto.ArtifactRepositoryDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ArtifactRepositoryService;
import io.quarkus.logging.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.UUID;

/**
 * 制品仓库管理 API
 */
@Tag(name = "制品仓库管理", description = "制品仓库配置管理 API")
@Path("/api/deployment/repositories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtifactRepositoryResource {

    @Inject
    ArtifactRepositoryService repositoryService;

    @Inject
    AuditLogService auditLogService;

    @Context
    SecurityContext securityContext;

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Operation(summary = "查询仓库列表", description = "分页查询制品仓库列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回仓库列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public PageResultDTO<ArtifactRepositoryDTO> list(
            @Parameter(description = "仓库类型", in = ParameterIn.QUERY) @QueryParam("type") ArtifactRepository.RepositoryType type,
            @Parameter(description = "是否激活", in = ParameterIn.QUERY) @QueryParam("active") Boolean active,
            @Parameter(description = "关键词", in = ParameterIn.QUERY) @QueryParam("keyword") String keyword,
            @Parameter(description = "页码", in = ParameterIn.QUERY) @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @Parameter(description = "每页数量", in = ParameterIn.QUERY) @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return repositoryService.listRepositories(pageNum, pageSize, type, active, keyword);
    }

    @GET
    @Path("/type/{type}")
    @Operation(summary = "按类型查询仓库", description = "根据类型查询制品仓库列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回仓库列表"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public List<ArtifactRepositoryDTO> listByType(@Parameter(description = "仓库类型", in = ParameterIn.PATH) @PathParam("type") ArtifactRepository.RepositoryType type) {
        return repositoryService.getByType(type);
    }

    @GET
    @Path("/default/{type}")
    @Operation(summary = "获取默认仓库", description = "获取指定类型的默认制品仓库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回默认仓库"),
        @ApiResponse(responseCode = "404", description = "默认仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ArtifactRepositoryDTO getDefault(@PathParam("type") ArtifactRepository.RepositoryType type) {
        ArtifactRepositoryDTO dto = repositoryService.getDefaultRepository(type);
        if (dto == null) {
            throw new WebApplicationException("No default repository found for type: " + type, Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "获取仓库详情", description = "根据 ID 查询制品仓库详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回仓库详情"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ArtifactRepositoryDTO get(@Parameter(description = "仓库 ID") @PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @Path("/name/{name}")
    @Operation(summary = "按名称查询仓库", description = "根据名称查询制品仓库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回仓库详情"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:view")
    public ArtifactRepositoryDTO getByName(@Parameter(description = "仓库名称") @PathParam("name") String name) {
        ArtifactRepositoryDTO dto = repositoryService.getByName(name);
        if (dto == null) {
            throw new WebApplicationException("Repository not found: " + name, Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @Operation(summary = "创建仓库", description = "创建新的制品仓库配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "成功创建仓库"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足"),
        @ApiResponse(responseCode = "409", description = "仓库已存在")
    })
    @RequiresPermission("deployment:create")
    public Response create(ArtifactRepositoryDTO dto) {
        try {
            ArtifactRepositoryDTO created = repositoryService.create(dto);
            recordAuditLog(AuditAction.CREATE_ARTIFACT_REPOSITORY, AuditResult.SUCCESS,
                    "创建制品仓库：" + created.name, "ArtifactRepository", created.id);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "更新仓库", description = "更新制品仓库配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功更新仓库"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "400", description = "请求参数无效"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足"),
        @ApiResponse(responseCode = "409", description = "仓库名称冲突")
    })
    @RequiresPermission("deployment:edit")
    public ArtifactRepositoryDTO update(@Parameter(description = "仓库 ID") @PathParam("id") UUID id, ArtifactRepositoryDTO dto) {
        try {
            ArtifactRepositoryDTO updated = repositoryService.update(id, dto);
            if (updated == null) {
                throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
            }
            recordAuditLog(AuditAction.UPDATE_ARTIFACT_REPOSITORY, AuditResult.SUCCESS,
                    "更新制品仓库：" + updated.name, "ArtifactRepository", id);
            return updated;
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "删除仓库", description = "删除制品仓库配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "成功删除仓库"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:delete")
    public Response delete(@Parameter(description = "仓库 ID") @PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.getById(id);
        boolean deleted = repositoryService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DELETE_ARTIFACT_REPOSITORY, AuditResult.SUCCESS,
                "删除制品仓库：" + (dto != null ? dto.name : id), "ArtifactRepository", id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/activate")
    @Operation(summary = "激活仓库", description = "激活制品仓库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功激活仓库"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public ArtifactRepositoryDTO activate(@Parameter(description = "仓库 ID") @PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.setActive(id, true);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.ACTIVATE_RESOURCE, AuditResult.SUCCESS,
                "激活制品仓库：" + dto.name, "ArtifactRepository", id);
        return dto;
    }

    @POST
    @Path("/{id}/deactivate")
    @Operation(summary = "停用仓库", description = "停用制品仓库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功停用仓库"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public ArtifactRepositoryDTO deactivate(@Parameter(description = "仓库 ID") @PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.setActive(id, false);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.DEACTIVATE_RESOURCE, AuditResult.SUCCESS,
                "停用制品仓库：" + dto.name, "ArtifactRepository", id);
        return dto;
    }

    @POST
    @Path("/{id}/default")
    @Operation(summary = "设为默认", description = "设置默认制品仓库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功设置默认仓库"),
        @ApiResponse(responseCode = "404", description = "仓库不存在"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @RequiresPermission("deployment:edit")
    public ArtifactRepositoryDTO setDefault(@Parameter(description = "仓库 ID") @PathParam("id") UUID id) {
        ArtifactRepositoryDTO dto = repositoryService.setDefault(id);
        if (dto == null) {
            throw new WebApplicationException("Repository not found", Response.Status.NOT_FOUND);
        }
        recordAuditLog(AuditAction.SET_DEFAULT_RESOURCE, AuditResult.SUCCESS,
                "设置默认制品仓库：" + dto.name, "ArtifactRepository", id);
        return dto;
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
            auditLogService.log(username, null, action, result, description, resourceType, resourceId, clientIp, "/api/deployment/repositories");
        } catch (Exception e) {
            Log.warnf("Failed to record audit log: %s", e.getMessage());
        }
    }
}