package com.easystation.deployment.resource;

import com.easystation.deployment.dto.DeploymentVersionDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.enums.VersionStatus;
import com.easystation.deployment.service.DeploymentVersionService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * 部署版本资源
 */
@Path("/api/deployments/versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class DeploymentVersionResource {
    
    @Inject
    DeploymentVersionService versionService;
    
    /**
     * 查询版本列表
     * GET /api/deployments/versions
     */
    @GET
    public PageResultDTO<DeploymentVersionDTO> listVersions(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("status") VersionStatus status,
            @QueryParam("releaseId") UUID releaseId,
            @QueryParam("keyword") String keyword,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder) {
        
        return versionService.listVersions(pageNum, pageSize, applicationId, status, releaseId, keyword, sortBy, sortOrder);
    }
    
    /**
     * 获取版本详情
     * GET /api/deployments/versions/{id}
     */
    @GET
    @Path("/{id}")
    public DeploymentVersionDTO getVersion(@PathParam("id") UUID id) {
        return versionService.getVersion(id);
    }
    
    /**
     * 创建版本
     * POST /api/deployments/versions
     */
    @POST
    public Response createVersion(@Valid DeploymentVersionDTO dto) {
        // TODO: 从安全上下文获取当前用户
        String createdBy = "system";
        DeploymentVersionDTO created = versionService.createVersion(dto, createdBy);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    /**
     * 更新版本
     * PUT /api/deployments/versions/{id}
     */
    @PUT
    @Path("/{id}")
    public DeploymentVersionDTO updateVersion(@PathParam("id") UUID id, @Valid DeploymentVersionDTO dto) {
        return versionService.updateVersion(id, dto);
    }
    
    /**
     * 删除版本
     * DELETE /api/deployments/versions/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteVersion(@PathParam("id") UUID id) {
        versionService.deleteVersion(id);
        return Response.noContent().build();
    }
    
    /**
     * 版本比对
     * POST /api/deployments/versions/compare
     */
    @POST
    @Path("/compare")
    public Object compareVersions(
            @QueryParam("fromVersionId") UUID fromVersionId,
            @QueryParam("toVersionId") UUID toVersionId) {
        return versionService.compareVersions(fromVersionId, toVersionId);
    }
}