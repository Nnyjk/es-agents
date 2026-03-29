package com.easystation.search.resource;

import com.easystation.search.domain.SearchResult;
import com.easystation.search.domain.SearchSuggestion;
import com.easystation.search.domain.UserSearchHistory;
import com.easystation.search.service.SearchHistoryService;
import com.easystation.search.service.SearchService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 搜索 API Resource
 */
@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class SearchResource {

    @Inject
    SearchService searchService;

    @Inject
    SearchHistoryService searchHistoryService;

    @Context
    SecurityContext securityContext;

    /**
     * 统一搜索
     * GET /api/search?q=keyword&types=host,deployment&limit=20&offset=0
     */
    @GET
    public Response search(
            @QueryParam("q") String query,
            @QueryParam("types") String types,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @Context HttpHeaders headers) {
        
        if (query == null || query.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Query parameter 'q' is required"))
                .build();
        }

        List<SearchResult> results = searchService.search(query, types, limit, offset);
        
        // 记录搜索历史（如果有用户）
        UUID userId = getCurrentUserId(securityContext);
        if (userId != null) {
            searchHistoryService.recordSearch(userId, query, results.size());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("total", results.size());
        response.put("results", results);

        return Response.ok(response).build();
    }

    /**
     * 获取搜索建议
     * GET /api/search/suggest?q=key&limit=10
     */
    @GET
    @Path("/suggest")
    public Response getSuggestions(
            @QueryParam("q") String query,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        
        if (query == null || query.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Query parameter 'q' is required"))
                .build();
        }

        List<SearchSuggestion> suggestions = searchService.getSuggestions(query, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("suggestions", suggestions);

        return Response.ok(response).build();
    }

    /**
     * 获取搜索历史
     * GET /api/search/history?limit=20
     */
    @GET
    @Path("/history")
    public Response getHistory(
            @QueryParam("limit") @DefaultValue("20") int limit,
            @Context HttpHeaders headers) {
        
        UUID userId = getCurrentUserId(securityContext);
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Authentication required"))
                .build();
        }

        List<UserSearchHistory> history = searchHistoryService.getHistory(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("history", history);

        return Response.ok(response).build();
    }

    /**
     * 删除单条搜索历史
     * DELETE /api/search/history/{id}
     */
    @DELETE
    @Path("/history/{id}")
    public Response deleteHistory(
            @PathParam("id") String historyId,
            @Context HttpHeaders headers) {
        
        UUID userId = getCurrentUserId(securityContext);
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Authentication required"))
                .build();
        }

        try {
            UUID id = UUID.fromString(historyId);
            boolean deleted = searchHistoryService.deleteHistory(id);
            
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "History not found"))
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid history ID"))
                .build();
        }
    }

    /**
     * 清空搜索历史
     * DELETE /api/search/history
     */
    @DELETE
    @Path("/history")
    public Response clearHistory(@Context HttpHeaders headers) {
        
        UUID userId = getCurrentUserId(securityContext);
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Authentication required"))
                .build();
        }

        searchHistoryService.clearHistory(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "History cleared");

        return Response.ok(response).build();
    }

    /**
     * 从 SecurityContext 获取当前用户 ID
     */
    private UUID getCurrentUserId(SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        if (username != null && !username.isBlank()) {
            // 从用户名获取用户 ID（实际项目中可通过 UserRepository 查询）
            // 这里简化处理，返回 null 表示需要认证
            return null;
        }
        return null;
    }
}
