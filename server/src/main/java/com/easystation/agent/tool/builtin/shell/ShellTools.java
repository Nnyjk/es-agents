package com.easystation.agent.tool.builtin.shell;

import com.easystation.agent.tool.domain.ToolParameter;
import com.easystation.agent.tool.spi.Tool;
import com.easystation.agent.tool.spi.ToolExecutionResult;
import io.quarkus.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Shell 工具
 * 提供执行 Shell 命令的能力
 */
public class ShellTools implements Tool {

    @Override
    public String getId() {
        return "shell.execute";
    }

    @Override
    public String getName() {
        return "执行 Shell 命令";
    }

    @Override
    public String getDescription() {
        return "在服务器上执行 Shell 命令，返回标准输出、标准错误和退出码";
    }

    @Override
    public List<ToolParameter> getParameters() {
        List<ToolParameter> params = new ArrayList<>();

        ToolParameter command = new ToolParameter();
        command.name = "command";
        command.type = "string";
        command.description = "要执行的 Shell 命令";
        command.required = true;
        params.add(command);

        ToolParameter workingDir = new ToolParameter();
        workingDir.name = "workingDir";
        workingDir.type = "string";
        workingDir.description = "工作目录（可选，默认为当前目录）";
        workingDir.required = false;
        params.add(workingDir);

        ToolParameter timeout = new ToolParameter();
        timeout.name = "timeout";
        timeout.type = "number";
        timeout.description = "超时时间（毫秒，默认 30000）";
        timeout.required = false;
        params.add(timeout);

        return params;
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> params) {
        long startTime = System.currentTimeMillis();

        try {
            String command = getStringParam(params, "command");
            if (command == null || command.trim().isEmpty()) {
                return ToolExecutionResult.failed("Command is required");
            }

            String workingDir = getStringParam(params, "workingDir");
            long timeout = getLongParam(params, "timeout", 30000);

            Log.infof("Executing shell command: %s (timeout: %dms)", command, timeout);

            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            if (workingDir != null && !workingDir.isEmpty()) {
                pb.directory(new java.io.File(workingDir));
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待完成
            boolean completed = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return ToolExecutionResult.failed("Command execution timeout after " + timeout + "ms");
            }

            int exitCode = process.exitValue();
            long durationMs = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("stdout", output.toString());
            result.put("exitCode", exitCode);
            result.put("durationMs", durationMs);

            if (exitCode != 0) {
                return ToolExecutionResult.builder()
                        .status(com.easystation.agent.tool.domain.ToolExecutionStatus.FAILED)
                        .output(result)
                        .error("Command exited with code " + exitCode)
                        .durationMs(durationMs)
                        .build();
            }

            return ToolExecutionResult.success(result, durationMs);

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            Log.errorf(e, "Shell command execution failed");
            return ToolExecutionResult.failed(e.getMessage(), durationMs);
        }
    }

    @Override
    public long getDefaultTimeout() {
        return 60000; // 60 秒
    }

    private String getStringParam(Map<String, Object> params, String name) {
        Object value = params.get(name);
        return value != null ? value.toString() : null;
    }

    private long getLongParam(Map<String, Object> params, String name, long defaultValue) {
        Object value = params.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
