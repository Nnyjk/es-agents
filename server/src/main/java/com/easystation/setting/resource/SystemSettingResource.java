package com.easystation.setting.resource;

import com.easystation.setting.dto.SettingRecord;
import com.easystation.setting.service.SystemSettingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SystemSettingResource {

    @Inject
    SystemSettingService settingService;

    @GET
    public Response list(@QueryParam("category") String category) {
        return Response.ok(settingService.list(category)).build();
    }

    @GET
    @Path("/{key}")
    public Response get(@PathParam("key") String key) {
        return Response.ok(settingService.get(key)).build();
    }

    @PUT
    @Path("/{key}")
    public Response update(@PathParam("key") String key, @Valid SettingRecord.Update dto) {
        SettingRecord.Update updateDto = new SettingRecord.Update(key, dto.value());
        return Response.ok(settingService.update(updateDto)).build();
    }

    @POST
    @Path("/batch")
    public Response batchUpdate(@Valid SettingRecord.BatchUpdate dto) {
        settingService.batchUpdate(dto);
        return Response.ok().build();
    }

    @GET
    @Path("/all")
    public Response getAllSettings() {
        return Response.ok(settingService.getAllSettings()).build();
    }

    @GET
    @Path("/basic")
    public Response getBasicSettings() {
        return Response.ok(settingService.getBasicSettings()).build();
    }

    @PUT
    @Path("/basic")
    public Response updateBasicSettings(@Valid SettingRecord.BasicSettings dto) {
        return Response.ok(settingService.updateBasicSettings(dto)).build();
    }

    @GET
    @Path("/security")
    public Response getSecuritySettings() {
        return Response.ok(settingService.getSecuritySettings()).build();
    }

    @PUT
    @Path("/security")
    public Response updateSecuritySettings(@Valid SettingRecord.SecuritySettings dto) {
        return Response.ok(settingService.updateSecuritySettings(dto)).build();
    }

    @GET
    @Path("/alert")
    public Response getAlertSettings() {
        return Response.ok(settingService.getAlertSettings()).build();
    }

    @PUT
    @Path("/alert")
    public Response updateAlertSettings(@Valid SettingRecord.AlertSettings dto) {
        return Response.ok(settingService.updateAlertSettings(dto)).build();
    }

    @POST
    @Path("/init")
    public Response initializeDefaultSettings() {
        settingService.initializeDefaultSettings();
        return Response.ok().build();
    }
}