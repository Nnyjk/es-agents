package com.easystation.agent.memory;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryImportance;
import com.easystation.agent.memory.domain.MemoryType;
import com.easystation.agent.memory.domain.Session;
import com.easystation.agent.memory.impl.MemoryServiceImpl;
import com.easystation.agent.memory.repository.MemoryRepository;
import com.easystation.agent.memory.repository.SessionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 记忆框架集成测试
 */
@QuarkusTest
@Transactional
public class MemoryFrameworkTest {

    @Inject
    MemoryServiceImpl memoryService;

    @Inject
    MemoryRepository memoryRepository;

    @Inject
    SessionRepository sessionRepository;

    @Test
    public void testCreateSession() {
        // 创建会话
        Session session = memoryService.createSession("test-agent", "test-user");
        
        assertNotNull(session);
        assertNotNull(session.sessionId);
        assertEquals("test-agent", session.agentId);
        assertEquals("test-user", session.userId);
        assertFalse(session.archived);
        
        // 清理
        memoryService.cleanupSession(session.sessionId);
    }

    @Test
    public void testShortTermMemory() {
        // 创建会话
        Session session = memoryService.createSession("test-agent-stm", "test-user");
        String sessionId = session.sessionId;
        
        // 添加短期记忆
        memoryService.addShortTermMemory(sessionId, "记忆内容 1");
        memoryService.addShortTermMemory(sessionId, "记忆内容 2");
        memoryService.addShortTermMemory(sessionId, "记忆内容 3");
        
        // 获取短期记忆
        List<Memory> memories = memoryService.getShortTermMemories(sessionId, 10);
        
        assertEquals(3, memories.size());
        assertEquals(MemoryType.SHORT_TERM, memories.get(0).type);
        assertEquals("记忆内容 3", memories.get(0).content);
        
        // 清理
        memoryService.cleanupSession(sessionId);
    }

    @Test
    public void testLongTermMemory() {
        // 创建会话
        Session session = memoryService.createSession("test-agent-ltm", "test-user");
        String sessionId = session.sessionId;
        
        // 添加长期记忆
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadata.put("tags", "demo");
        
        memoryService.addLongTermMemory(sessionId, "重要内容 1", metadata, MemoryImportance.HIGH);
        memoryService.addLongTermMemory(sessionId, "重要内容 2", null, MemoryImportance.MEDIUM);
        
        // 获取长期记忆
        List<Memory> memories = memoryService.getLongTermMemories(sessionId, 10);
        
        assertEquals(2, memories.size());
        assertEquals(MemoryType.LONG_TERM, memories.get(0).type);
        
        // 搜索长期记忆
        List<Memory> searchResults = memoryService.searchLongTermMemories(sessionId, "重要", 10);
        assertEquals(2, searchResults.size());
        
        // 清理
        memoryService.cleanupSession(sessionId);
    }

    @Test
    public void testContextVariables() {
        // 创建会话
        Session session = memoryService.createSession("test-agent-ctx", "test-user");
        String sessionId = session.sessionId;
        
        // 设置上下文变量
        memoryService.setContextVariable(sessionId, "user_name", "张三");
        memoryService.setContextVariable(sessionId, "user_role", "admin");
        
        // 获取上下文变量
        String userName = memoryService.getContextVariable(sessionId, "user_name");
        assertEquals("张三", userName);
        
        Map<String, String> allContext = memoryService.getAllContextVariables(sessionId);
        assertEquals(2, allContext.size());
        assertEquals("admin", allContext.get("user_role"));
        
        // 清理
        memoryService.cleanupSession(sessionId);
    }

    @Test
    public void testMemoryCompression() {
        // 创建会话
        Session session = memoryService.createSession("test-agent-compress", "test-user");
        String sessionId = session.sessionId;
        
        // 添加多条低重要性记忆
        for (int i = 0; i < 15; i++) {
            Memory memory = new Memory();
            memory.sessionId = sessionId;
            memory.type = MemoryType.SHORT_TERM;
            memory.content = "测试记忆 " + i;
            memory.importance = MemoryImportance.LOW;
            memory.createdAt = LocalDateTime.now().minusHours(13); // 超过 12 小时
            memoryRepository.persist(memory);
        }
        
        // 执行压缩
        memoryService.autoCompressMemories(sessionId);
        
        // 验证压缩结果
        List<Memory> remaining = memoryService.getShortTermMemories(sessionId, 100);
        assertTrue(remaining.size() < 15, "应该有记忆被压缩");
        
        // 清理
        memoryService.cleanupSession(sessionId);
    }

    @Test
    public void testSessionLifecycle() {
        // 创建会话
        Session session = memoryService.createSession("test-agent-lifecycle", "test-user");
        String sessionId = session.sessionId;
        
        // 获取会话
        Session retrieved = memoryService.getSession(sessionId);
        assertNotNull(retrieved);
        assertEquals(sessionId, retrieved.sessionId);
        
        // 归档会话
        memoryService.archiveSession(sessionId);
        Session archived = sessionRepository.findBySessionId(sessionId);
        assertTrue(archived.archived);
        
        // 清理
        memoryService.cleanupSession(sessionId);
        
        // 验证已删除
        Session deleted = sessionRepository.findBySessionId(sessionId);
        assertNull(deleted);
    }
}
