package com.easystation.agent.tool.impl;

import com.easystation.agent.tool.domain.ToolDefinition;
import com.easystation.agent.tool.domain.ToolParameter;
import com.easystation.agent.tool.domain.ToolStatus;
import com.easystation.agent.tool.repository.ToolDefinitionRepository;
import com.easystation.agent.tool.repository.ToolParameterRepository;
import com.easystation.agent.tool.spi.Tool;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具注册表测试
 */
@QuarkusTest
public class ToolRegistryImplTest {

    @Inject
    ToolRegistryImpl toolRegistry;

    @Inject
    ToolDefinitionRepository toolDefinitionRepository;

    @Inject
    ToolParameterRepository toolParameterRepository;

    /**
     * 测试注册工具
     */
    @Test
    public void testRegisterTool() {
        // 创建测试工具
        TestTool testTool = new TestTool();
        
        // 注册工具
        toolRegistry.register(testTool);
        
        // 验证工具已注册
        ToolDefinition definition = toolDefinitionRepository.findByToolId("test.tool");
        assertNotNull(definition);
        assertEquals("Test Tool", definition.name);
        assertEquals(ToolStatus.ACTIVE, definition.status);
        
        // 验证参数已保存
        List<ToolParameter> parameters = toolParameterRepository.findByTool(definition.id);
        assertEquals(1, parameters.size());
        assertEquals("param1", parameters.get(0).name);
    }

    /**
     * 测试获取工具
     */
    @Test
    public void testGetTool() {
        Tool tool = toolRegistry.getTool("shell.execute");
        assertNotNull(tool, "Should get shell.execute tool");
        assertEquals("shell.execute", tool.getId());
    }

    /**
     * 测试获取不存在的工具
     */
    @Test
    public void testGetNonExistentTool() {
        Tool tool = toolRegistry.getTool("non.existent");
        assertNull(tool, "Should return null for non-existent tool");
    }

    /**
     * 测试列出所有工具
     */
    @Test
    public void testListTools() {
        List<ToolDefinition> tools = toolRegistry.listTools();
        assertTrue(tools.size() >= 3, "Should have at least 3 built-in tools");
    }

    /**
     * 测试按状态列出工具
     */
    @Test
    public void testListToolsByStatus() {
        List<ToolDefinition> activeTools = toolRegistry.listTools(ToolStatus.ACTIVE);
        assertFalse(activeTools.isEmpty(), "Should have active tools");
        
        List<ToolDefinition> deprecatedTools = toolRegistry.listTools(ToolStatus.DEPRECATED);
        // 可能没有 deprecated 工具
        assertNotNull(deprecatedTools);
    }

    /**
     * 测试注销工具
     */
    @Test
    public void testUnregisterTool() {
        // 先注册
        TestTool testTool = new TestTool();
        toolRegistry.register(testTool);
        
        // 验证已注册
        ToolDefinition before = toolDefinitionRepository.findByToolId("test.tool");
        assertNotNull(before);
        
        // 注销
        toolRegistry.unregister("test.tool");
        
        // 验证已标记为 DEPRECATED
        ToolDefinition after = toolDefinitionRepository.findByToolId("test.tool");
        assertNotNull(after);
        assertEquals(ToolStatus.DEPRECATED, after.status);
    }

    /**
     * 测试工具是否可用
     */
    @Test
    public void testIsToolAvailable() {
        assertTrue(toolRegistry.isToolAvailable("shell.execute"), "shell.execute should be available");
        assertFalse(toolRegistry.isToolAvailable("non.existent"), "non.existent should not be available");
    }

    /**
     * 测试工具
     */
    public static class TestTool implements Tool {
        @Override
        public String getId() {
            return "test.tool";
        }

        @Override
        public String getName() {
            return "Test Tool";
        }

        @Override
        public String getDescription() {
            return "A test tool";
        }

        @Override
        public List<ToolParameter> getParameters() {
            ToolParameter param = new ToolParameter();
            param.name = "param1";
            param.type = "string";
            param.description = "Test parameter";
            param.required = true;
            return List.of(param);
        }
    }
}
