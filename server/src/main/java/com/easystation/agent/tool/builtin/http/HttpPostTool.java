package com.easystation.agent.tool.builtin.http;

import com.easystation.agent.tool.domain.ToolParameter;
import com.easystation.agent.tool.spi.Tool;
import com.easystation.agent.tool.spi.ToolExecutionResult;
import io.quarkus.logging.Log;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * HTTP POST 工具
 * 执行 HTTP POST 请求
 */
public class HttpPostTool implements Tool {

    @Override
    public String getId() {
        return "http.post";
    }

    @Override
    public String getName() {
        return "HTTP POST 请求";
    }

    @Override
    public String getDescription() {
        return "执行 HTTP POST 请求，支持 JSON 和表单数据";
    }

    @Override
    public List<ToolParameter> getParameters() {
        List<ToolParameter> params = new ArrayList<>();

        ToolParameter url = new ToolParameter();
        url.name = "url";
        url.type = "string";
        url.description = "请求 URL";
        url.required = true;
        params.add(url);

        ToolParameter body = new ToolParameter();
        body.name = "body";
        body.type = "object";
        body.description = "请求体（JSON 对象或字符串）";
        body.required = false;
        params.add(body);

        ToolParameter contentType = new ToolParameter();
        contentType.name = "contentType";
        contentType.type = "string";
        contentType.description = "Content-Type (默认 application/json)";
        contentType.required = false;
        params.add(contentType);

        ToolParameter headers = new ToolParameter();
        headers.name = "headers";
        headers.type = "object";
        headers.description = "请求头（JSON 对象）";
        headers.required = false;
        params.add(headers);

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
            String url = getStringParam(params, "url");
            if (url == null || url.trim().isEmpty()) {
                return ToolExecutionResult.failed("URL is required");
            }

            Object body = params.get("body");
            String contentType = getStringParam(params, "contentType", "application/json");
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) params.get("headers");
            long timeout = getLongParam(params, "timeout", 30000);

            Log.infof("Executing HTTP POST: %s", url);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeout))
                    .build();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeout))
                    .POST(body != null ? HttpRequest.BodyPublishers.ofString(bodyToString(body))
                                       : HttpRequest.BodyPublishers.noBody());

            builder.header("Content-Type", contentType);
            if (headers != null) {
                headers.forEach(builder::header);
            }

            HttpResponse<String> response = client.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            long durationMs = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", response.statusCode());
            result.put("headers", response.headers().map());
            result.put("body", response.body());
            result.put("durationMs", durationMs);

            if (response.statusCode() >= 400) {
                return ToolExecutionResult.builder()
                        .status(com.easystation.agent.tool.domain.ToolExecutionStatus.FAILED)
                        .output(result)
                        .error("HTTP " + response.statusCode())
                        .durationMs(durationMs)
                        .build();
            }

            return ToolExecutionResult.success(result, durationMs);

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            Log.errorf(e, "HTTP POST request failed");
            return ToolExecutionResult.failed(e.getMessage(), durationMs);
        }
    }

    private String getStringParam(Map<String, Object> params, String name) {
        Object value = params.get(name);
        return value != null ? value.toString() : null;
    }

    private String getStringParam(Map<String, Object> params, String name, String defaultValue) {
        Object value = params.get(name);
        return value != null ? value.toString() : defaultValue;
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

    private String bodyToString(Object body) {
        if (body instanceof String) {
            return (String) body;
        }
        try {
            return com.fasterxml.jackson.databind.ObjectMapperCompat.writeValueAsString(body);
        } catch (Exception e) {
            return body.toString();
        }
    }
}
