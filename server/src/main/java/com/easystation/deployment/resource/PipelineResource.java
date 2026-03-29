package com.easystation.deployment.resource;

import com.easystation.auth.annotation.RequiresPermission;
import com.easystation.deployment.dto.*;
import com.easystation.deployment.enums.PipelineStatus;
import com.easystation.deployment.service.PipelineService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/deployment/pipelines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PipelineResource {

    @Inject
    PipelineService pipelineService;

    @GET
    @RequiresPermission("deployment:view")
    public PageResultDTO<PipelineDTO> list(
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("name") String name,
            @QueryParam("applicationId") UUID applicationId,
            @QueryParam("status") PipelineStatus status) {
        return pipelineService.listPipelines(pageNum, pageSize, name, applicationId, status);
    }

    @GET
    @Path("/{id}")
    @RequiresPermission("deployment:view")
    public PipelineDTO get(@PathParam("id") UUID id) {
        return pipelineService.getPipeline(id);
    }

    @POST
    @RequiresPermission("deployment:create")
    public PipelineDTO create(PipelineDTO dto) {
        return pipelineService.createPipeline(dto);
    }

    @PUT
    @Path("/{id}")
    @RequiresPermission("deployment:edit")
    public PipelineDTO update(@PathParam("id") UUID id, PipelineDTO dto) {
        return pipelineService.updatePipeline(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @RequiresPermission("deployment:delete")
    public Response delete(@PathParam("id") UUID id) {
        pipelineService.deletePipeline(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/trigger")
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO trigger(@PathParam("id") UUID id, 
                                        @QueryParam("triggeredBy") @DefaultValue("system") String triggeredBy) {
        return pipelineService.triggerPipeline(id, triggeredBy);
    }

    @GET
    @Path("/{id}/executions")
    @RequiresPermission("deployment:view")
    public List<PipelineExecutionDTO> getExecutions(@PathParam("id") UUID id) {
        return pipelineService.getPipelineExecutions(id);
    }

    @GET
    @Path("/executions/{executionId}")
    @RequiresPermission("deployment:view")
    public PipelineExecutionDTO getExecutionDetail(@PathParam("executionId") UUID executionId) {
        return pipelineService.getExecutionDetail(executionId);
    }

    @POST
    @Path("/executions/{executionId}/cancel")
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO cancelExecution(@PathParam("executionId") UUID executionId) {
        return pipelineService.cancelPipelineExecution(executionId);
    }

    @POST
    @Path("/executions/{executionId}/retry")
    @RequiresPermission("deployment:execute")
    public PipelineExecutionDTO retryExecution(@PathParam("executionId") UUID executionId) {
        return pipelineService.retryPipelineExecution(executionId);
    }
}