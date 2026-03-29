package com.easystation.agent.tool.spi;

import com.easystation.agent.tool.domain.ToolParameter;

import java.util.List;
import java.util.Map;

/**
 * 工具接口
 * 所有工具必须实现此接口
 */
public interface Tool {

    /**
     * 工具唯一标识（如：shell.execute）
     */
    String getId();

    /**
     * 工具名称
     */
    String getName();

    /**
     * 工具描述
     */
    String getDescription();

    /**
     * 参数定义列表
     */
    List<ToolParameter> getParameters();

    /**
     * 执行工具
     * @param params 参数键值对
     * @return 执行结果
     */
    ToolExecutionResult execute(Map<String, Object> params);

    /**
     * 是否支持异步执行
     */
    default boolean supportsAsync() {
        return false;
    }

    /**
     * 默认超时时间（毫秒）
     */
    default long getDefaultTimeout() {
        return 30000; // 30 秒
    }
}
