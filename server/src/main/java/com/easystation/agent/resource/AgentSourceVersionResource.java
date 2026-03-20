package com.easystation.agent.resource;

import com.easystation.agent.dto.AgentSourceVersionRecord;
import com.easystation.agent.service.AgentSourceVersionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/agents/sources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentSourceVersionResource {

    @Inject
    AgentSourceVersionService versionService;

    // Version endpoints
    @GET
    @Path("/{sourceId}/versions")
    public Response listVersions(
            @PathParam("sourceId") UUID sourceId,
            @QueryParam("version") String version,
            @QueryParam("verified") Boolean verified,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        AgentSourceVersionRecord.Query query = new AgentSourceVersionRecord.Query(
                sourceId, version, verified, limit, offset
        );
        return Response.ok(versionService.listVersions(query)).build();
    }

    @GET
    @Path("/versions/{id}")
    public Response getVersion(@PathParam("id") UUID id) {
        return Response.ok(versionService.getVersion(id)).build();
    }

    @POST
    @Path("/{sourceId}/versions")
    public Response createVersion(
            @PathParam("sourceId") UUID sourceId,
            @Valid AgentSourceVersionRecord.Create dto) {
        AgentSourceVersionRecord.Create createDto = new AgentSourceVersionRecord.Create(
                sourceId, dto.version(), dto.filePath(), dto.fileSize(),
                dto.checksumMd5(), dto.checksumSha256(), dto.description(),
                dto.downloadUrl(), dto.createdBy()
        );
        return Response.status(Response.Status.CREATED)
                .entity(versionService.createVersion(createDto))
                .build();
    }

    @PUT
    @Path("/versions/{id}")
    public Response updateVersion(@PathParam("id") UUID id, @Valid AgentSourceVersionRecord.Update dto) {
        return Response.ok(versionService.updateVersion(id, dto)).build();
    }

    @DELETE
    @Path("/versions/{id}")
    public Response deleteVersion(@PathParam("id") UUID id) {
        versionService.deleteVersion(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/versions/{id}/verify")
    public Response verifyVersion(@PathParam("id") UUID id, @Valid AgentSourceVersionRecord.VerifyRequest dto) {
        return Response.ok(versionService.verifyVersion(id, dto)).build();
    }

    // Cache endpoints
    @GET
    @Path("/{sourceId}/cache")
    public Response listCache(
            @PathParam("sourceId") UUID sourceId,
            @QueryParam("valid") Boolean valid,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        AgentSourceVersionRecord.CacheQuery query = new AgentSourceVersionRecord.CacheQuery(
                sourceId, valid, limit, offset
        );
        return Response.ok(versionService.listCache(query)).build();
    }

    @POST
    @Path("/{sourceId}/pull")
    public Response pull(
            @PathParam("sourceId") UUID sourceId,
            @Valid AgentSourceVersionRecord.PullRequest dto) {
        AgentSourceVersionRecord.PullRequest pullDto = new AgentSourceVersionRecord.PullRequest(
                sourceId, dto.version(), dto.useCache(), dto.pulledBy()
        );
        return Response.ok(versionService.pull(pullDto)).build();
    }

    @DELETE
    @Path("/cache/{cacheId}")
    public Response invalidateCache(@PathParam("cacheId") UUID cacheId) {
        versionService.invalidateCache(cacheId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{sourceId}/cache")
    public Response clearCache(@PathParam("sourceId") UUID sourceId) {
        versionService.clearCache(sourceId);
        return Response.noContent().build();
    }
}