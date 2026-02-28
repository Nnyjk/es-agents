package com.easystation.infra.socket;

import io.quarkus.logging.Log;
import jakarta.websocket.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AgentClientEndpoint extends Endpoint {

    private Session session;
    private final Consumer<String> onMessageCallback;
    private final Consumer<Session> onCloseCallback;
    private final BiConsumer<Session, Boolean> onConnectCallback;

    public AgentClientEndpoint(Consumer<String> onMessageCallback, Consumer<Session> onCloseCallback) {
        this(onMessageCallback, onCloseCallback, null);
    }

    public AgentClientEndpoint(Consumer<String> onMessageCallback, Consumer<Session> onCloseCallback, 
                                BiConsumer<Session, Boolean> onConnectCallback) {
        this.onMessageCallback = onMessageCallback;
        this.onCloseCallback = onCloseCallback;
        this.onConnectCallback = onConnectCallback;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        Log.debugf("Connected to Agent: %s", session.getId());
        
        if (onConnectCallback != null) {
            onConnectCallback.accept(session, true);
        }
        
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                if (onMessageCallback != null) {
                    onMessageCallback.accept(message);
                }
            }
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        Log.debugf("Connection closed: %s", closeReason);
        if (onCloseCallback != null) {
            onCloseCallback.accept(session);
        }
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        Log.errorf("Connection error: %s", throwable.getMessage());
        if (onConnectCallback != null) {
            onConnectCallback.accept(session, false);
        }
    }

    public void sendText(String text) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(text);
        }
    }
}
