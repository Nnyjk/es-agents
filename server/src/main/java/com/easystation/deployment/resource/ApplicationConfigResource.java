package com.easystation.deployment.resource;

import com.easystation.deployment.domain.ApplicationConfig;
import com.easystation.deployment.dto.ApplicationConfigDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.ApplicationConfigService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * 应用配置管理 API
 */
@Path("/api/deployment/applications/{applicationId}/configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationConfigResource {

    @Inject
    ApplicationConfigService configService;

    @GET
    public PageResultDTO<ApplicationConfigDTO> list(
            @PathParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("configType") ApplicationConfig.ConfigType configType,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return configService.listConfigs(pageNum, pageSize, applicationId, environmentId, configType);
    }

    @GET
    @Path("/{id}")
    public ApplicationConfigDTO get(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        ApplicationConfigDTO dto = configService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    public Response create(
            @PathParam("applicationId") UUID applicationId,
            ApplicationConfigDTO dto) {
        dto.applicationId = applicationId;
        ApplicationConfigDTO created = configService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public ApplicationConfigDTO update(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id,
            ApplicationConfigDTO dto) {
        ApplicationConfigDTO updated = configService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        return updated;
    }

    @DELETE
    @Path("/{id}")
    public Response delete(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        boolean deleted = configService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Config not found", Response.Status.NOT_FOUND);
        }
        return Response.noContent().build();
    }

    @DELETE
    public Response deleteByApplication(@PathParam("applicationId") UUID applicationId) {
        int count = configService.deleteByApplication(applicationId);
        return Response.ok().entity("{\"deleted\": " + count + "}").build();
    }
}