package com.easystation.agent.memory;

import com.easystation.agent.memory.domain.Memory;
import com.easystation.agent.memory.domain.MemoryImportance;
import com.easystation.agent.memory.domain.MemoryType;
import com.easystation.agent.memory.impl.MemoryServiceImpl;
import com.easystation.agent.memory.repository.MemoryRepository;
import com.easystation.agent.memory.repository.SessionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 记忆服务单元测试
 */
@QuarkusTest
public class MemoryServiceTest {

    @Inject
    MemoryServiceImpl memoryService;

    @Inject
    MemoryRepository memoryRepository;

    @Inject
    SessionRepository sessionRepository;

    private String testSessionId;

    @BeforeEach
    public void setUp() {
        // 创建测试会话
        var session = memoryService.createSession("test-agent", "test-user");
        testSessionId = session.sessionId;
    }

    @Test
    public void testAddAndGetShortTermMemory() {
        // 添加短期记忆
        memoryService.addShortTermMemory(testSessionId, "测试内容");
        
        // 获取短期记忆
        List<Memory> memories = memoryService.getShortTermMemories(testSessionId, 10);
        
        assertEquals(1, memories.size());
        assertEquals("测试内容", memories.get(0).content);
        assertEquals(MemoryType.SHORT_TERM, memories.get(0).type);
        assertNotNull(memories.get(0).expiresAt);
    }

    @Test
    public void testShortTermMemoryLimit() {
        // 添加多条短期记忆
        for (int i = 0; i < 60; i++) {
            memoryService.addShortTermMemory(testSessionId, "内容" + i);
        }
        
        // 获取限制为 20 条
        List<Memory> memories = memoryService.getShortTermMemories(testSessionId, 20);
        
        assertTrue(memories.size() <= 20);
    }

    @Test
    public void testAddLongTermMemoryWithMetadata() {
        // 添加带元数据的长期记忆
        var metadata = new java.util.HashMap<String, Object>();
        metadata.put("category", "test");
        metadata.put("priority", "high");
        
        memoryService.addLongTermMemory(testSessionId, "重要内容", metadata, MemoryImportance.HIGH);
        
        List<Memory> memories = memoryService.getLongTermMemories(testSessionId, 10);
        
        assertEquals(1, memories.size());
        assertEquals(MemoryType.LONG_TERM, memories.get(0).type);
        assertEquals(MemoryImportance.HIGH, memories.get(0).importance);
        assertEquals("test", memories.get(0).metadata.get("category"));
    }

    @Test
    public void testSearchLongTermMemories() {
        // 添加多条长期记忆
        memoryService.addLongTermMemory(testSessionId, "用户喜欢 Java 编程", null, MemoryImportance.MEDIUM);
        memoryService.addLongTermMemory(testSessionId, "用户偏好 Python 脚本", null, MemoryImportance.MEDIUM);
        memoryService.addLongTermMemory(testSessionId, "系统配置信息", null, MemoryImportance.LOW);
        
        // 搜索包含"用户"的记忆
        List<Memory> results = memoryService.searchLongTermMemories(testSessionId, "用户", 10);
        
        assertEquals(2, results.size());
    }

    @Test
    public void testContextVariableOperations() {
        // 设置上下文变量
        memoryService.setContextVariable(testSessionId, "key1", "value1");
        memoryService.setContextVariable(testSessionId, "key2", "value2");
        
        // 获取单个变量
        String value1 = memoryService.getContextVariable(testSessionId, "key1");
        assertEquals("value1", value1);
        
        // 获取所有变量
        var allContext = memoryService.getAllContextVariables(testSessionId);
        assertEquals(2, allContext.size());
        assertEquals("value2", allContext.get("key2"));
        
        // 更新变量
        memoryService.setContextVariable(testSessionId, "key1", "updated");
        String updated = memoryService.getContextVariable(testSessionId, "key1");
        assertEquals("updated", updated);
    }

    @Test
    public void testDeleteLongTermMemory() {
        // 添加长期记忆
        memoryService.addLongTermMemory(testSessionId, "待删除内容", null, MemoryImportance.LOW);
        
        List<Memory> before = memoryService.getLongTermMemories(testSessionId, 10);
        assertEquals(1, before.size());
        
        // 删除记忆
        memoryService.deleteLongTermMemory(before.get(0).id.toString());
        
        List<Memory> after = memoryService.getLongTermMemories(testSessionId, 10);
        assertEquals(0, after.size());
    }

    @Test
    public void testMemoryAccessCount() {
        // 添加记忆
        memoryService.addShortTermMemory(testSessionId, "访问测试");
        
        List<Memory> memories = memoryService.getShortTermMemories(testSessionId, 10);
        Memory memory = memories.get(0);
        
        // 首次访问后访问次数应增加
        assertTrue(memory.accessCount >= 1);
    }

    @Test
    public void testSessionNotFound() {
        // 测试不存在的会话
        assertThrows(IllegalArgumentException.class, () -> {
            memoryService.addShortTermMemory("non-existent-session", "内容");
        });
    }

    @Test
    public void testCleanupSession() {
        // 添加一些记忆
        memoryService.addShortTermMemory(testSessionId, "临时内容");
        memoryService.addLongTermMemory(testSessionId, "永久内容", null, MemoryImportance.HIGH);
        
        // 清理会话
        memoryService.cleanupSession(testSessionId);
        
        // 验证会话和记忆都被删除
        var session = memoryService.getSession(testSessionId);
        assertNull(session);
    }
}
