package com.easystation.agent.dto;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    private boolean success;
    private String message;
    private Map<String, Object> metadata;

    public ValidationResult() {
        this.metadata = new HashMap<>();
    }

    public ValidationResult(boolean success, String message, Map<String, Object> metadata) {
        this.success = success;
        this.message = message;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public static ValidationResult success(String message, Map<String, Object> metadata) {
        return new ValidationResult(true, message, metadata);
    }

    public static ValidationResult success(String message) {
        return new ValidationResult(true, message, null);
    }

    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message, null);
    }
}
