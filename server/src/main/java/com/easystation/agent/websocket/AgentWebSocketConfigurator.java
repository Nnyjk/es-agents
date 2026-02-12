package com.easystation.agent.websocket;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

public class AgentWebSocketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> headers = request.getHeaders();
        if (headers.containsKey("X-Agent-Secret")) {
            sec.getUserProperties().put("X-Agent-Secret", headers.get("X-Agent-Secret").get(0));
        }
    }
}
