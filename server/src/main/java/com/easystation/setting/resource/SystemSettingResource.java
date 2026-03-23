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

    @GET
    @Path("/storage")
    public Response getStorageSettings() {
        return Response.ok(settingService.getStorageSettings()).build();
    }

    @PUT
    @Path("/storage")
    public Response updateStorageSettings(@Valid SettingRecord.StorageSettings dto) {
        return Response.ok(settingService.updateStorageSettings(dto)).build();
    }

    @GET
    @Path("/resource")
    public Response getResourceSettings() {
        return Response.ok(settingService.getResourceSettings()).build();
    }

    @PUT
    @Path("/resource")
    public Response updateResourceSettings(@Valid SettingRecord.ResourceSettings dto) {
        return Response.ok(settingService.updateResourceSettings(dto)).build();
    }

    @GET
    @Path("/notification")
    public Response getNotificationSettings() {
        return Response.ok(settingService.getNotificationSettings()).build();
    }

    @PUT
    @Path("/notification")
    public Response updateNotificationSettings(@Valid SettingRecord.NotificationSettings dto) {
        return Response.ok(settingService.updateNotificationSettings(dto)).build();
    }

    @GET
    @Path("/maintenance")
    public Response getMaintenanceSettings() {
        return Response.ok(settingService.getMaintenanceSettings()).build();
    }

    @PUT
    @Path("/maintenance")
    public Response updateMaintenanceSettings(@Valid SettingRecord.MaintenanceSettings dto) {
        return Response.ok(settingService.updateMaintenanceSettings(dto)).build();
    }

    @GET
    @Path("/integration")
    public Response getIntegrationSettings() {
        return Response.ok(settingService.getIntegrationSettings()).build();
    }

    @PUT
    @Path("/integration")
    public Response updateIntegrationSettings(@Valid SettingRecord.IntegrationSettings dto) {
        return Response.ok(settingService.updateIntegrationSettings(dto)).build();
    }
}