package com.easystation.infra.resource;

import com.easystation.infra.record.HostRecord;
import com.easystation.infra.service.HostService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Path("/infra/hosts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HostResource {

    @Inject
    HostService hostService;

    @GET
    public Response list(@QueryParam("envId") UUID envId) {
        return Response.ok(hostService.list(envId)).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(hostService.get(id)).build();
    }

    @POST
    public Response create(@Valid HostRecord.Create dto) {
        return Response.status(Response.Status.CREATED).entity(hostService.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, HostRecord.Update dto) {
        return Response.ok(hostService.update(id, dto)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        hostService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/connect")
    public Response connect(@PathParam("id") UUID id) {
        hostService.connect(id);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/install-guide")
    public Response getInstallGuide(@PathParam("id") UUID id) {
        return Response.ok(hostService.getInstallGuide(id)).build();
    }

    @GET
    @Path("/{id}/package")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPackage(@PathParam("id") UUID id, @QueryParam("sourceId") UUID sourceId) {
        if (sourceId == null) {
            throw new WebApplicationException("sourceId is required", Response.Status.BAD_REQUEST);
        }
        
        StreamingOutput stream = hostService.downloadPackage(id, sourceId);
        
        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"host-agent-" + id + ".zip\"")
                .build();
    }
}
