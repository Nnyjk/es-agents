package com.easystation.agent.collaboration.dto;

public class CreateSessionRequest {
    public String name;
    public String description;
    public String[] agentIds;
    public String creatorAgentId;

    /**
     * 验证请求参数
     */
    public boolean validate() {
        return name != null && !name.trim().isEmpty();
    }
}
