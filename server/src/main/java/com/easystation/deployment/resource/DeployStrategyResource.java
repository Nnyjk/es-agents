package com.easystation.deployment.resource;

import com.easystation.deployment.domain.DeployStrategy;
import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.deployment.dto.DeployStrategyDTO;
import com.easystation.deployment.dto.PageResultDTO;
import com.easystation.deployment.service.DeployStrategyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * 部署策略管理 API
 */
@Path("/api/deployment/applications/{applicationId}/strategies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeployStrategyResource {

    @Inject
    DeployStrategyService strategyService;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<DeployStrategyDTO> list(
            @PathParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId,
            @QueryParam("type") DeployStrategy.StrategyType type,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return strategyService.listStrategies(pageNum, pageSize, applicationId, environmentId, type);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/all")
    public List<DeployStrategyDTO> listAll(@PathParam("applicationId") UUID applicationId) {
        return strategyService.getByApplicationId(applicationId);
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/default")
    public DeployStrategyDTO getDefault(
            @PathParam("applicationId") UUID applicationId,
            @QueryParam("environmentId") UUID environmentId) {
        DeployStrategyDTO dto = strategyService.getDefaultStrategy(applicationId, environmentId);
        if (dto == null) {
            throw new WebApplicationException("No default strategy found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @GET
    @RequiresPermission("deployment:view")
    @Path("/{id}")
    public DeployStrategyDTO get(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.getById(id);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    public Response create(
            @PathParam("applicationId") UUID applicationId,
            DeployStrategyDTO dto) {
        dto.applicationId = applicationId;
        DeployStrategyDTO created = strategyService.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @RequiresPermission("deployment:edit")
    @Path("/{id}")
    public DeployStrategyDTO update(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id,
            DeployStrategyDTO dto) {
        DeployStrategyDTO updated = strategyService.update(id, dto);
        if (updated == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return updated;
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    @Path("/{id}")
    public Response delete(
            @PathParam("applicationId") UUID applicationId,
            @PathParam("id") UUID id) {
        boolean deleted = strategyService.delete(id);
        if (!deleted) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return Response.noContent().build();
    }

    @DELETE
    @RequiresPermission("deployment:delete")
    public Response deleteByApplication(@PathParam("applicationId") UUID applicationId) {
        long count = strategyService.deleteByApplication(applicationId);
        return Response.ok().entity("{\"deleted\": " + count + "}").build();
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/activate")
    public DeployStrategyDTO activate(@PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.setActive(id, true);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/deactivate")
    public DeployStrategyDTO deactivate(@PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.setActive(id, false);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }

    @POST
    @RequiresPermission("deployment:create")
    @Path("/{id}/default")
    public DeployStrategyDTO setDefault(@PathParam("id") UUID id) {
        DeployStrategyDTO dto = strategyService.setDefault(id);
        if (dto == null) {
            throw new WebApplicationException("Strategy not found", Response.Status.NOT_FOUND);
        }
        return dto;
    }
}