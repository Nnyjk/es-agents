package com.easystation.agent.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具执行请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionRequest {

    /** 工具 ID */
    private String toolId;

    /** 参数键值对 */
    private Map<String, Object> parameters;

    /** 关联的任务 ID */
    private String taskId;

    /** 超时时间（毫秒） */
    private Long timeout;
}
