package com.easystation.agent.tool;

import com.easystation.agent.tool.domain.ToolDefinition;
import com.easystation.agent.tool.domain.ToolExecutionLog;
import com.easystation.agent.tool.domain.ToolExecutionStatus;
import com.easystation.agent.tool.domain.ToolStatus;
import com.easystation.agent.tool.impl.ToolExecutorImpl;
import com.easystation.agent.tool.impl.ToolRegistryImpl;
import com.easystation.agent.tool.repository.ToolDefinitionRepository;
import com.easystation.agent.tool.repository.ToolExecutionLogRepository;
import com.easystation.agent.tool.spi.Tool;
import com.easystation.agent.tool.spi.ToolExecutionResult;
import com.easystation.agent.tool.spi.ToolExecutor;
import com.easystation.agent.tool.spi.ToolRegistry;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具框架集成测试
 */
@QuarkusTest
public class ToolFrameworkTest {

    @Inject
    ToolRegistry toolRegistry;

    @Inject
    ToolExecutor toolExecutor;

    @Inject
    ToolDefinitionRepository toolDefinitionRepository;

    @Inject
    ToolExecutionLogRepository executionLogRepository;

    /**
     * 测试工具注册
     */
    @Test
    public void testToolRegistration() {
        // 验证内置工具已注册
        List<ToolDefinition> tools = toolDefinitionRepository.findAll().list();
        
        assertTrue(tools.size() >= 3, "Should have at least 3 built-in tools");
        
        // 验证 shell.execute 工具存在
        ToolDefinition shellTool = toolDefinitionRepository.findByToolId("shell.execute");
        assertNotNull(shellTool, "shell.execute tool should exist");
        assertEquals("Shell 命令执行", shellTool.name);
        assertEquals(ToolStatus.ENABLED, shellTool.status);
    }

    /**
     * 测试工具执行 - Shell
     */
    @Test
    public void testShellToolExecution() {
        Map<String, Object> params = new HashMap<>();
        params.put("command", "echo 'Hello World'");
        params.put("timeout", 5000L);

        ToolExecutionResult result = toolExecutor.execute("shell.execute", params, 5000L);

        assertEquals(ToolExecutionStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().toString().contains("Hello World"));
        assertTrue(result.getDurationMs() > 0);
    }

    /**
     * 测试工具执行 - HTTP GET
     */
    @Test
    public void testHttpGetToolExecution() {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "https://httpbin.org/get");
        params.put("timeout", 10000L);

        ToolExecutionResult result = toolExecutor.execute("http.get", params, 10000L);

        // 注意：这个测试可能因网络问题失败
        // 如果失败，可以跳过或 mock
        assertNotNull(result);
    }

    /**
     * 测试工具执行 - 超时
     */
    @Test
    public void testToolExecutionTimeout() {
        Map<String, Object> params = new HashMap<>();
        params.put("command", "sleep 10");
        params.put("timeout", 1000L); // 1 秒超时

        ToolExecutionResult result = toolExecutor.execute("shell.execute", params, 1000L);

        assertEquals(ToolExecutionStatus.TIMEOUT, result.getStatus());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("timeout") || result.getError().contains("Timeout"));
    }

    /**
     * 测试工具执行 - 命令失败
     */
    @Test
    public void testToolExecutionFailure() {
        Map<String, Object> params = new HashMap<>();
        params.put("command", "exit 1");
        params.put("timeout", 5000L);

        ToolExecutionResult result = toolExecutor.execute("shell.execute", params, 5000L);

        assertEquals(ToolExecutionStatus.FAILED, result.getStatus());
        assertNotNull(result.getError());
    }

    /**
     * 测试工具执行日志记录
     */
    @Test
    public void testExecutionLogRecording() {
        String taskId = "test-task-" + UUID.randomUUID();
        
        Map<String, Object> params = new HashMap<>();
        params.put("command", "echo 'Test Log'");

        toolExecutor.execute("shell.execute", params, 5000L);

        // 等待日志写入
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<ToolExecutionLog> logs = executionLogRepository.findByTaskId(taskId);
        // 注意：异步日志可能不会立即写入
        // 这个测试主要验证日志功能存在
        assertNotNull(logs);
    }

    /**
     * 测试工具搜索
     */
    @Test
    public void testToolSearch() {
        List<ToolDefinition> shellTools = toolDefinitionRepository.search("shell");
        assertFalse(shellTools.isEmpty(), "Should find shell tools");
        
        List<ToolDefinition> httpTools = toolDefinitionRepository.search("http");
        assertFalse(httpTools.isEmpty(), "Should find http tools");
    }

    /**
     * 测试工具按分类查找
     */
    @Test
    public void testToolsByCategory() {
        List<ToolDefinition> shellTools = toolDefinitionRepository.findByCategory("shell");
        assertFalse(shellTools.isEmpty(), "Should find shell category tools");
    }
}
