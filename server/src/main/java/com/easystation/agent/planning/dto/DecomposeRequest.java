package com.easystation.agent.planning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 任务分解请求
 */
public record DecomposeRequest(
    @NotBlank(message = "目标描述不能为空")
    @Size(max = 2000, message = "目标描述最大2000字符")
    String goal,

    @Min(value = 1, message = "最大分解深度至少为1")
    @Max(value = 5, message = "最大分解深度不能超过5")
    int maxDepth,

    String context,

    List<String> constraints
) {
    /**
     * 简化的分解请求（使用默认最大深度）
     */
    public DecomposeRequest(String goal) {
        this(goal, 3, null, null);
    }

    /**
     * 带上下文的分解请求
     */
    public DecomposeRequest(String goal, String context) {
        this(goal, 3, context, null);
    }
}