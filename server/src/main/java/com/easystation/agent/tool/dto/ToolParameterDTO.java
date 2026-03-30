package com.easystation.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 工具参数 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolParameterDTO {

    private UUID id;
    private String name;
    private String type;
    private String description;
    private boolean required;
    private String defaultValue;
    private String validationRule;
    private int sortOrder;
}
