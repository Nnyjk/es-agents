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
 * HTTP GET 工具
 * 执行 HTTP GET 请求
 */
public class HttpGetTool implements Tool {

    @Override
    public String getId() {
        return "http.get";
    }

    @Override
    public String getName() {
        return "HTTP GET 请求";
    }

    @Override
    public String getDescription() {
        return "执行 HTTP GET 请求，返回响应状态码、头信息和响应体";
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

            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) params.get("headers");
            long timeout = getLongParam(params, "timeout", 30000);

            Log.infof("Executing HTTP GET: %s", url);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeout))
                    .build();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeout))
                    .GET();

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
            Log.errorf(e, "HTTP GET request failed");
            return ToolExecutionResult.failed(e.getMessage(), durationMs);
        }
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
