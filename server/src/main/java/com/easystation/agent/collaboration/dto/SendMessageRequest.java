package com.easystation.agent.collaboration.dto;

import com.easystation.agent.collaboration.domain.MessageType;

public class SendMessageRequest {
    public Long sessionId;
    public MessageType type;
    public String fromAgentId;
    public String toAgentId;
    public Long correlationId;
    public String subject;
    public String content;
    public String metadata;
}
