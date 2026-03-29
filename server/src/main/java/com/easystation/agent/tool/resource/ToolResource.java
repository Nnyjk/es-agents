package com.easystation.agent.tool.resource;

import com.easystation.agent.tool.domain.ToolDefinition;
import com.easystation.agent.tool.domain.ToolParameter;
import com.easystation.agent.tool.domain.ToolStatus;
import com.easystation.agent.tool.dto.ToolDefinitionDTO;
import com.easystation.agent.tool.dto.ToolParameterDTO;
import com.easystation.agent.tool.repository.ToolDefinitionRepository;
import com.easystation.agent.tool.repository.ToolParameterRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工具管理 REST API
 */
@Path("/api/agent/tools")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ToolResource {

    @Inject
    ToolDefinitionRepository toolDefinitionRepository;

    @Inject
    ToolParameterRepository toolParameterRepository;

    /**
     * 列出所有工具
     */
    @GET
    public List<ToolDefinitionDTO> listTools(@QueryParam("status") ToolStatus status) {
        List<ToolDefinition> definitions;
        if (status != null) {
            definitions = toolDefinitionRepository.find("status", status).list();
        } else {
            definitions = toolDefinitionRepository.findAll().list();
        }
        return definitions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取工具详情
     */
    @GET
    @Path("/{id}")
    public ToolDefinitionDTO getTool(@RestPath UUID id) {
        ToolDefinition definition = toolDefinitionRepository.findById(id);
        if (definition == null) {
            throw new NotFoundException("Tool not found: " + id);
        }
        return toDTO(definition);
    }

    /**
     * 根据 toolId 获取工具
     */
    @GET
    @Path("/by-id/{toolId}")
    public ToolDefinitionDTO getToolByToolId(@RestPath String toolId) {
        ToolDefinition definition = toolDefinitionRepository.findByToolId(toolId);
        if (definition == null) {
            throw new NotFoundException("Tool not found: " + toolId);
        }
        return toDTO(definition);
    }

    /**
     * 搜索工具
     */
    @GET
    @Path("/search")
    public List<ToolDefinitionDTO> searchTools(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Query parameter 'q' is required");
        }
        return toolDefinitionRepository.search(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按分类查找工具
     */
    @GET
    @Path("/category/{category}")
    public List<ToolDefinitionDTO> getToolsByCategory(@RestPath String category) {
        return toolDefinitionRepository.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新工具状态
     */
    @PUT
    @Path("/{id}/status")
    @Transactional
    public ToolDefinitionDTO updateStatus(@RestPath UUID id, Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            throw new BadRequestException("Status is required");
        }

        ToolStatus status = ToolStatus.valueOf(statusStr.toUpperCase());
        toolDefinitionRepository.updateStatus(id, status);

        ToolDefinition definition = toolDefinitionRepository.findById(id);
        if (definition == null) {
            throw new NotFoundException("Tool not found: " + id);
        }
        return toDTO(definition);
    }

    /**
     * 删除工具（软删除，标记为 DEPRECATED）
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTool(@RestPath UUID id) {
        ToolDefinition definition = toolDefinitionRepository.findById(id);
        if (definition == null) {
            throw new NotFoundException("Tool not found: " + id);
        }

        definition.status = ToolStatus.DEPRECATED;
        toolDefinitionRepository.persist(definition);

        return Response.noContent().build();
    }

    /**
     * 转换为 DTO
     */
    private ToolDefinitionDTO toDTO(ToolDefinition definition) {
        List<ToolParameterDTO> parameters = toolParameterRepository.findByTool(definition.id)
                .stream()
                .map(this::toParameterDTO)
                .collect(Collectors.toList());

        return ToolDefinitionDTO.builder()
                .id(definition.id)
                .toolId(definition.toolId)
                .name(definition.name)
                .description(definition.description)
                .category(definition.category)
                .version(definition.version)
                .status(definition.status)
                .createdAt(definition.createdAt)
                .updatedAt(definition.updatedAt)
                .parameters(parameters)
                .build();
    }

    /**
     * 参数转换为 DTO
     */
    private ToolParameterDTO toParameterDTO(ToolParameter parameter) {
        return ToolParameterDTO.builder()
                .id(parameter.id)
                .name(parameter.name)
                .type(parameter.type)
                .description(parameter.description)
                .required(parameter.required)
                .defaultValue(parameter.defaultValue)
                .validationRule(parameter.validationRule)
                .order(parameter.order)
                .build();
    }
}
