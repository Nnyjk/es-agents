package com.easystation.agent.tool.dto;

import com.easystation.agent.tool.domain.ToolStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 工具定义 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinitionDTO {

    private UUID id;
    private String toolId;
    private String name;
    private String description;
    private String category;
    private String version;
    private ToolStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ToolParameterDTO> parameters;
}
