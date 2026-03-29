package com.easystation.agent.tool.builtin.file;

import com.easystation.agent.tool.domain.ToolParameter;
import com.easystation.agent.tool.spi.Tool;
import com.easystation.agent.tool.spi.ToolExecutionResult;
import io.quarkus.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件工具
 * 提供文件列表、创建、删除、复制、移动等操作
 */
public class FileTools implements Tool {

    @Override
    public String getId() {
        return "file.list";
    }

    @Override
    public String getName() {
        return "列出目录内容";
    }

    @Override
    public String getDescription() {
        return "列出指定目录的内容，支持递归和过滤";
    }

    @Override
    public List<ToolParameter> getParameters() {
        List<ToolParameter> params = new ArrayList<>();

        ToolParameter path = new ToolParameter();
        path.name = "path";
        path.type = "string";
        path.description = "目录路径";
        path.required = true;
        params.add(path);

        ToolParameter recursive = new ToolParameter();
        recursive.name = "recursive";
        recursive.type = "boolean";
        recursive.description = "是否递归列出子目录（默认 false）";
        recursive.required = false;
        params.add(recursive);

        ToolParameter filter = new ToolParameter();
        filter.name = "filter";
        filter.type = "string";
        filter.description = "文件过滤模式（如：*.java）";
        filter.required = false;
        params.add(filter);

        return params;
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> params) {
        long startTime = System.currentTimeMillis();

        try {
            String path = getStringParam(params, "path");
            if (path == null || path.trim().isEmpty()) {
                return ToolExecutionResult.failed("Path is required");
            }

            boolean recursive = getBooleanParam(params, "recursive", false);
            String filter = getStringParam(params, "filter");

            Log.infof("Listing directory: %s (recursive: %s)", path, recursive);

            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                return ToolExecutionResult.failed("Path does not exist: " + path);
            }
            if (!Files.isDirectory(dirPath)) {
                return ToolExecutionResult.failed("Path is not a directory: " + path);
            }

            List<Map<String, Object>> files = new ArrayList<>();
            if (recursive) {
                Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (matchesFilter(file, filter)) {
                            files.add(toFileInfo(file, attrs));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                try (Stream<Path> stream = Files.list(dirPath)) {
                    stream.filter(p -> matchesFilter(p, filter))
                            .forEach(p -> {
                                try {
                                    files.add(toFileInfo(p, Files.readAttributes(p, BasicFileAttributes.class)));
                                } catch (IOException e) {
                                    Log.warnf(e, "Failed to read file attributes: %s", p);
                                }
                            });
                }
            }

            long durationMs = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("path", path);
            result.put("count", files.size());
            result.put("files", files);
            result.put("durationMs", durationMs);

            return ToolExecutionResult.success(result, durationMs);

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            Log.errorf(e, "Failed to list directory");
            return ToolExecutionResult.failed(e.getMessage(), durationMs);
        }
    }

    private boolean matchesFilter(Path path, String filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        String fileName = path.getFileName().toString();
        if (filter.startsWith("*")) {
            return fileName.endsWith(filter.substring(1));
        }
        return fileName.equals(filter);
    }

    private Map<String, Object> toFileInfo(Path path, BasicFileAttributes attrs) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", path.getFileName().toString());
        info.put("path", path.toString());
        info.put("isDirectory", attrs.isDirectory());
        info.put("size", attrs.isRegularFile() ? attrs.size() : 0);
        info.put("createdTime", attrs.creationTime().toString());
        info.put("modifiedTime", attrs.lastModifiedTime().toString());
        return info;
    }

    private String getStringParam(Map<String, Object> params, String name) {
        Object value = params.get(name);
        return value != null ? value.toString() : null;
    }

    private boolean getBooleanParam(Map<String, Object> params, String name, boolean defaultValue) {
        Object value = params.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
