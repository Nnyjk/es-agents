package com.easystation.agent.memory.spi;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryImportance;
import com.easystation.agent.memory.domain.Session;

import java.util.List;
import java.util.Map;

/**
 * 记忆服务接口
 */
public interface MemoryService {

    // ==================== 会话管理 ====================

    /**
     * 创建会话
     */
    Session createSession(String agentId, String userId);

    /**
     * 获取会话
     */
    Session getSession(String sessionId);

    /**
     * 更新会话最后访问时间
     */
    void touchSession(String sessionId);

    /**
     * 归档会话
     */
    void archiveSession(String sessionId);

    // ==================== 短期记忆 ====================

    /**
     * 添加短期记忆
     */
    void addShortTermMemory(String sessionId, String content);

    /**
     * 获取短期记忆（最近 N 条）
     */
    List<Memory> getShortTermMemories(String sessionId, int limit);

    /**
     * 清理过期短期记忆
     */
    void cleanupExpiredShortTermMemories();

    // ==================== 长期记忆 ====================

    /**
     * 添加长期记忆
     */
    void addLongTermMemory(String sessionId, String content, Map<String, Object> metadata, MemoryImportance importance);

    /**
     * 搜索长期记忆（基于关键词）
     */
    List<Memory> searchLongTermMemories(String sessionId, String query, int limit);

    /**
     * 获取长期记忆
     */
    List<Memory> getLongTermMemories(String sessionId, int limit);

    /**
     * 删除长期记忆
     */
    void deleteLongTermMemory(String memoryId);

    // ==================== 上下文管理 ====================

    /**
     * 设置上下文变量
     */
    void setContextVariable(String sessionId, String key, String value);

    /**
     * 获取上下文变量
     */
    String getContextVariable(String sessionId, String key);

    /**
     * 获取所有上下文变量
     */
    Map<String, String> getAllContextVariables(String sessionId);

    // ==================== 记忆压缩 ====================

    /**
     * 压缩记忆
     */
    String compressMemories(List<Memory> memories);

    /**
     * 执行记忆压缩（自动）
     */
    void autoCompressMemories(String sessionId);

    // ==================== 清理 ====================

    /**
     * 清理会话所有数据
     */
    void cleanupSession(String sessionId);
}
