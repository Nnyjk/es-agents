package com.easystation.plugin.marketplace.resource;

import com.easystation.plugin.marketplace.domain.PluginMarketplace;
import com.easystation.plugin.marketplace.dto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * 插件市场 REST API
 */
@Path("/api/plugins/marketplace")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PluginMarketplaceResource {
    
    @Inject
    PluginMarketplace pluginMarketplace;
    
    /**
     * 获取插件列表
     */
    @GET
    public Response listPlugins(
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("pageSize") @DefaultValue("20") int pageSize
    ) {
        PluginListResponse response = pluginMarketplace.listPlugins(page, pageSize);
        return Response.ok(response).build();
    }
    
    /**
     * 获取插件详情
     */
    @GET
    @Path("/{id}")
    public Response getPluginDetail(@PathParam("id") String pluginId) {
        try {
            PluginDetailResponse response = pluginMarketplace.getPluginDetail(pluginId);
            return Response.ok(response).build();
        } catch (PluginMarketplace.PluginNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Plugin not found: " + pluginId))
                .build();
        }
    }
    
    /**
     * 搜索插件
     */
    @GET
    @Path("/search")
    public Response searchPlugins(
        @QueryParam("keyword") String keyword,
        @QueryParam("category") String category,
        @QueryParam("page") @DefaultValue("1") int page,
        @QueryParam("pageSize") @DefaultValue("20") int pageSize,
        @QueryParam("sortBy") @DefaultValue("name") String sortBy,
        @QueryParam("sortOrder") @DefaultValue("asc") String sortOrder
    ) {
        PluginSearchRequest request = new PluginSearchRequest(
            keyword, category, null, page, pageSize, sortBy, sortOrder
        );
        PluginListResponse response = pluginMarketplace.searchPlugins(request);
        return Response.ok(response).build();
    }
    
    /**
     * 安装插件
     */
    @POST
    @Path("/install")
    public Response installPlugin(PluginInstallRequest request) {
        try {
            pluginMarketplace.installPlugin(request);
            return Response.ok(new MessageResponse("Plugin installation initiated"))
                .build();
        } catch (PluginMarketplace.PluginNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }
    
    /**
     * 卸载插件
     */
    @DELETE
    @Path("/{id}")
    public Response uninstallPlugin(@PathParam("id") String pluginId) {
        try {
            pluginMarketplace.uninstallPlugin(pluginId);
            return Response.ok(new MessageResponse("Plugin uninstalled successfully"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to uninstall plugin: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 评分插件
     */
    @POST
    @Path("/{id}/rate")
    public Response ratePlugin(
        @PathParam("id") String pluginId,
        RateRequest request
    ) {
        try {
            pluginMarketplace.ratePlugin(pluginId, request.rating(), request.comment());
            return Response.ok(new MessageResponse("Rating submitted successfully"))
                .build();
        } catch (PluginMarketplace.PluginNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }
    
    // 辅助记录类
    
    public record ErrorResponse(String message) {}
    
    public record MessageResponse(String message) {}
    
    public record RateRequest(int rating, String comment) {}
}
