package client

import (
	"fmt"
	"net/http"
	"net/url"
	"sync"

	"github.com/gorilla/websocket"
)

type WSClient struct {
	ServerURL string
	SecretKey string
	AgentID   string
	Conn      *websocket.Conn
	mu        sync.Mutex
}

func NewWS(serverURL, secretKey, agentID string) *WSClient {
	return &WSClient{
		ServerURL: serverURL,
		SecretKey: secretKey,
		AgentID:   agentID,
	}
}

func (c *WSClient) Connect() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.Conn != nil {
		return nil
	}

	// Convert http/https to ws/wss
	u, err := url.Parse(c.ServerURL)
	if err != nil {
		return err
	}
	scheme := "ws"
	if u.Scheme == "https" {
		scheme = "wss"
	}

	// Assuming Server endpoint is /ws/agent/{agentId}
	u.Scheme = scheme
	u.Path = fmt.Sprintf("/ws/agent/%s", c.AgentID)

	header := http.Header{}
	// Note: Standard WebSocket handshake doesn't support custom headers easily in all servers/proxies,
	// but X-Agent-Secret is good practice if supported.
	if c.SecretKey != "" {
		header.Set("X-Agent-Secret", c.SecretKey)
	}

	conn, _, err := websocket.DefaultDialer.Dial(u.String(), header)
	if err != nil {
		return err
	}

	c.Conn = conn
	return nil
}

func (c *WSClient) SendHeartbeat(req HeartbeatRequest) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.Conn == nil {
		if err := c.Connect(); err != nil {
			return err
		}
	}

	// Send raw HeartbeatRequest
	// Authentication is handled via X-Agent-Secret header during handshake
	return c.Conn.WriteJSON(req)
}

func (c *WSClient) SendLog(logMsg string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.Conn == nil {
		return fmt.Errorf("not connected")
	}

	msg := map[string]interface{}{
		"type":    "LOG",
		"content": logMsg, // Server expects JSON string or raw? Server code checks contains("type":"LOG")
		// Let's send a JSON object
	}

	// Server expects a string message. If we send JSON, it will parse it.
	// Our Server logic: if (message.contains("\"type\":\"LOG\"")) broadcastLog(message)
	// So sending JSON is fine.

	return c.Conn.WriteJSON(msg)
}

func (c *WSClient) Close() {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.Conn != nil {
		c.Conn.Close()
		c.Conn = nil
	}
}
