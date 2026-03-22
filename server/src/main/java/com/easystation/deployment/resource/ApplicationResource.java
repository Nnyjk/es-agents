package com.easystation.deployment.resource;

import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.ApplicationStatus;
import com.easystation.deployment.service.ApplicationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/deployment/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    ApplicationService applicationService;

    @GET
    public PageResultDTO<ApplicationDTO> list(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("name") String name,
            @QueryParam("project") String project,
            @QueryParam("owner") String owner,
            @QueryParam("status") ApplicationStatus status) {
        return applicationService.listApplications(pageNum, pageSize, name, project, owner, status);
    }

    @GET
    @Path("/{id}")
    public ApplicationDTO get(@PathParam("id") UUID id) {
        return applicationService.getApplication(id);
    }

    @POST
    public ApplicationDTO create(ApplicationDTO dto) {
        return applicationService.createApplication(dto);
    }

    @PUT
    @Path("/{id}")
    public ApplicationDTO update(@PathParam("id") UUID id, ApplicationDTO dto) {
        return applicationService.updateApplication(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        applicationService.deleteApplication(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/archive")
    public ApplicationDTO archive(@PathParam("id") UUID id) {
        return applicationService.archiveApplication(id);
    }
}