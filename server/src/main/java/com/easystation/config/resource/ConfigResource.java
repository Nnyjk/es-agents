package com.easystation.config.resource;

import com.easystation.config.dto.ConfigDTO;
import com.easystation.config.dto.ConfigHistoryDTO;
import com.easystation.config.dto.ConfigUpdateRequest;
import com.easystation.config.service.ConfigService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

/**
 * 配置管理 REST API
 * 
 * 提供配置的热重载管理功能
 */
@Path("/api/v1/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "config", description = "配置管理 API")
public class ConfigResource {

    @Inject
    ConfigService configService;

    /**
     * 获取所有配置
     */
    @GET
    @Operation(summary = "获取所有配置", description = "返回系统所有配置项")
    public List<ConfigDTO> getAllConfigs() {
        return configService.getAllConfigs();
    }

    /**
     * 根据 key 获取配置
     */
    @GET
    @Path("/{key}")
    @Operation(summary = "获取单个配置", description = "根据配置 key 获取配置详情")
    public ConfigDTO getConfigByKey(@PathParam("key") String configKey) {
        return configService.getConfigByKey(configKey);
    }

    /**
     * 更新配置
     */
    @PUT
    @Path("/{key}")
    @RolesAllowed({"ADMIN", "OPERATOR"})
    @Operation(summary = "更新配置", description = "更新指定配置项的值，触发热重载")
    public ConfigDTO updateConfig(
            @PathParam("key") String configKey,
            ConfigUpdateRequest request,
            @Context SecurityContext securityContext) {
        
        String currentUser = getCurrentUser(securityContext);
        return configService.updateConfig(configKey, request, currentUser);
    }

    /**
     * 创建配置
     */
    @POST
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "创建配置", description = "创建新的配置项")
    public ConfigDTO createConfig(
            @QueryParam("key") String configKey,
            ConfigUpdateRequest request,
            @Context SecurityContext securityContext) {
        
        String currentUser = getCurrentUser(securityContext);
        return configService.createConfig(configKey, request, currentUser);
    }

    /**
     * 删除配置
     */
    @DELETE
    @Path("/{key}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "删除配置", description = "删除指定的配置项")
    public void deleteConfig(@PathParam("key") String configKey) {
        configService.deleteConfig(configKey);
    }

    /**
     * 获取配置变更历史
     */
    @GET
    @Path("/{key}/history")
    @RolesAllowed({"ADMIN", "OPERATOR"})
    @Operation(summary = "获取配置变更历史", description = "返回指定配置的变更历史记录")
    public List<ConfigHistoryDTO> getConfigHistory(@PathParam("key") String configKey) {
        return configService.getHistory(configKey);
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser(SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal != null) {
            return principal.getName();
        }
        return "system";
    }
}
