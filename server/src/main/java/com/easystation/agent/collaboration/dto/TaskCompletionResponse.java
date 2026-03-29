package com.easystation.agent.collaboration.dto;

/**
 * 任务完成响应
 */
public class TaskCompletionResponse {

    public Long taskId;
    public String result;
    public boolean success;
    public String errorMessage;

    public boolean validate() {
        return taskId != null;
    }
}
