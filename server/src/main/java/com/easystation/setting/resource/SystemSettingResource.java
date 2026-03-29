package com.easystation.setting.resource;

import com.easystation.setting.dto.SettingRecord;
import com.easystation.setting.service.SystemSettingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "系统设置管理", description = "系统配置设置管理 API")
public class SystemSettingResource {

    @Inject
    SystemSettingService settingService;

    @GET
    @Operation(summary = "查询设置列表", description = "按分类查询系统设置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回设置列表"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "category", description = "设置分类", required = false)
    public Response list(@QueryParam("category") String category) {
        return Response.ok(settingService.list(category)).build();
    }

    @GET
    @Path("/{key}")
    @Operation(summary = "获取设置值", description = "获取指定键的系统设置值")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回设置值"),
        @APIResponse(responseCode = "404", description = "设置不存在"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "key", description = "设置键", required = true)
    public Response get(@PathParam("key") String key) {
        return Response.ok(settingService.get(key)).build();
    }

    @PUT
    @Path("/{key}")
    @Operation(summary = "更新设置值", description = "更新指定键的系统设置值")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "设置更新成功"),
        @APIResponse(responseCode = "404", description = "设置不存在"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    @Parameter(name = "key", description = "设置键", required = true)
    public Response update(@PathParam("key") String key, @Valid SettingRecord.Update dto) {
        SettingRecord.Update updateDto = new SettingRecord.Update(key, dto.value());
        return Response.ok(settingService.update(updateDto)).build();
    }

    @POST
    @Path("/batch")
    @Operation(summary = "批量更新设置", description = "批量更新多个系统设置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "设置批量更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response batchUpdate(@Valid SettingRecord.BatchUpdate dto) {
        settingService.batchUpdate(dto);
        return Response.ok().build();
    }

    @GET
    @Path("/all")
    @Operation(summary = "获取所有设置", description = "获取所有系统设置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回所有设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getAllSettings() {
        return Response.ok(settingService.getAllSettings()).build();
    }

    @GET
    @Path("/basic")
    @Operation(summary = "获取基础设置", description = "获取系统基础配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回基础设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getBasicSettings() {
        return Response.ok(settingService.getBasicSettings()).build();
    }

    @PUT
    @Path("/basic")
    @Operation(summary = "更新基础设置", description = "更新系统基础配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "基础设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateBasicSettings(@Valid SettingRecord.BasicSettings dto) {
        return Response.ok(settingService.updateBasicSettings(dto)).build();
    }

    @GET
    @Path("/security")
    @Operation(summary = "获取安全设置", description = "获取系统安全配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回安全设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getSecuritySettings() {
        return Response.ok(settingService.getSecuritySettings()).build();
    }

    @PUT
    @Path("/security")
    @Operation(summary = "更新安全设置", description = "更新系统安全配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "安全设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateSecuritySettings(@Valid SettingRecord.SecuritySettings dto) {
        return Response.ok(settingService.updateSecuritySettings(dto)).build();
    }

    @GET
    @Path("/alert")
    @Operation(summary = "获取告警设置", description = "获取系统告警配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回告警设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getAlertSettings() {
        return Response.ok(settingService.getAlertSettings()).build();
    }

    @PUT
    @Path("/alert")
    @Operation(summary = "更新告警设置", description = "更新系统告警配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "告警设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateAlertSettings(@Valid SettingRecord.AlertSettings dto) {
        return Response.ok(settingService.updateAlertSettings(dto)).build();
    }

    @POST
    @Path("/init")
    @Operation(summary = "初始化默认设置", description = "恢复系统默认设置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "默认设置初始化成功"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response initializeDefaultSettings() {
        settingService.initializeDefaultSettings();
        return Response.ok().build();
    }

    @GET
    @Path("/storage")
    @Operation(summary = "获取存储设置", description = "获取系统存储配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回存储设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getStorageSettings() {
        return Response.ok(settingService.getStorageSettings()).build();
    }

    @PUT
    @Path("/storage")
    @Operation(summary = "更新存储设置", description = "更新系统存储配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "存储设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateStorageSettings(@Valid SettingRecord.StorageSettings dto) {
        return Response.ok(settingService.updateStorageSettings(dto)).build();
    }

    @GET
    @Path("/resource")
    @Operation(summary = "获取资源配置", description = "获取系统资源配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回资源配置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getResourceSettings() {
        return Response.ok(settingService.getResourceSettings()).build();
    }

    @PUT
    @Path("/resource")
    @Operation(summary = "更新资源配置", description = "更新系统资源配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "资源配置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateResourceSettings(@Valid SettingRecord.ResourceSettings dto) {
        return Response.ok(settingService.updateResourceSettings(dto)).build();
    }

    @GET
    @Path("/notification")
    @Operation(summary = "获取通知设置", description = "获取系统通知配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回通知设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getNotificationSettings() {
        return Response.ok(settingService.getNotificationSettings()).build();
    }

    @PUT
    @Path("/notification")
    @Operation(summary = "更新通知设置", description = "更新系统通知配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "通知设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateNotificationSettings(@Valid SettingRecord.NotificationSettings dto) {
        return Response.ok(settingService.updateNotificationSettings(dto)).build();
    }

    @GET
    @Path("/maintenance")
    @Operation(summary = "获取维护设置", description = "获取系统维护配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回维护设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getMaintenanceSettings() {
        return Response.ok(settingService.getMaintenanceSettings()).build();
    }

    @PUT
    @Path("/maintenance")
    @Operation(summary = "更新维护设置", description = "更新系统维护配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "维护设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateMaintenanceSettings(@Valid SettingRecord.MaintenanceSettings dto) {
        return Response.ok(settingService.updateMaintenanceSettings(dto)).build();
    }

    @GET
    @Path("/integration")
    @Operation(summary = "获取集成设置", description = "获取系统集成配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "成功返回集成设置"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response getIntegrationSettings() {
        return Response.ok(settingService.getIntegrationSettings()).build();
    }

    @PUT
    @Path("/integration")
    @Operation(summary = "更新集成设置", description = "更新系统集成配置")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "集成设置更新成功"),
        @APIResponse(responseCode = "400", description = "请求参数无效"),
        @APIResponse(responseCode = "401", description = "未授权访问")
    })
    public Response updateIntegrationSettings(@Valid SettingRecord.IntegrationSettings dto) {
        return Response.ok(settingService.updateIntegrationSettings(dto)).build();
    }
}
