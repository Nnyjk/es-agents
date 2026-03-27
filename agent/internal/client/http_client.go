package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type Client struct {
	ServerURL string
	SecretKey string
	HTTP      *http.Client
}

type HeartbeatRequest struct {
	AgentID   string         `json:"agentId"`
	Status    string         `json:"status"`
	Timestamp time.Time      `json:"timestamp"`
	Version   string         `json:"version"`
	OsType    string         `json:"osType"`
	CPUUsage  float64        `json:"cpuUsage"`
	MemUsage  float64        `json:"memUsage"`
	DiskUsage float64        `json:"diskUsage"`
	Plugins   []PluginInfo   `json:"plugins,omitempty"`
}

// PluginInfo 用于心跳上报的插件信息
type PluginInfo struct {
	ID           string             `json:"id"`
	Name         string             `json:"name"`
	Version      string             `json:"version"`
	Description  string             `json:"description"`
	Capabilities []PluginCapability `json:"capabilities,omitempty"`
	Status       string             `json:"status"`
	RegisteredAt time.Time          `json:"registeredAt"`
	LastSeen     time.Time          `json:"lastSeen"`
}

// PluginCapability 插件能力描述
type PluginCapability struct {
	Name        string   `json:"name"`
	Description string   `json:"description"`
	Commands    []string `json:"commands,omitempty"`
}

type AgentTask struct {
	ID          string `json:"id"`
	CommandName string `json:"commandName"`
	Script      string `json:"script"`
	Args        string `json:"args"`
	Timeout     int64  `json:"timeout"`
}

func New(serverURL, secretKey string) *Client {
	return &Client{
		ServerURL: serverURL,
		SecretKey: secretKey,
		HTTP: &http.Client{
			Timeout: 5 * time.Second,
		},
	}
}

func (c *Client) SendHeartbeat(req HeartbeatRequest) error {
	body, err := json.Marshal(req)
	if err != nil {
		return err
	}

	httpReq, err := http.NewRequest("POST", c.ServerURL+"/api/gateway/heartbeat", bytes.NewBuffer(body))
	if err != nil {
		return err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	if c.SecretKey != "" {
		httpReq.Header.Set("X-Agent-Secret", c.SecretKey)
	}

	resp, err := c.HTTP.Do(httpReq)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("server returned status: %d", resp.StatusCode)
	}
	return nil
}

func (c *Client) FetchCommands(agentID string) ([]AgentTask, error) {
	httpReq, err := http.NewRequest("GET", fmt.Sprintf("%s/api/gateway/commands?agentId=%s", c.ServerURL, agentID), nil)
	if err != nil {
		return nil, err
	}
	if c.SecretKey != "" {
		httpReq.Header.Set("X-Agent-Secret", c.SecretKey)
	}

	resp, err := c.HTTP.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("server returned status: %d", resp.StatusCode)
	}

	var tasks []AgentTask
	if err := json.NewDecoder(resp.Body).Decode(&tasks); err != nil {
		return nil, err
	}
	return tasks, nil
}

// SendPluginHeartbeat 发送包含插件状态的心跳
// 插件状态会在心跳中上报到服务端
func (c *Client) SendPluginHeartbeat(req HeartbeatRequest) error {
	return c.SendHeartbeat(req)
}

// RegisterPlugin 向服务端注册插件
func (c *Client) RegisterPlugin(agentID string, plugin PluginInfo) error {
	body, err := json.Marshal(plugin)
	if err != nil {
		return err
	}

	httpReq, err := http.NewRequest("POST", fmt.Sprintf("%s/api/v1/agents/%s/plugins", c.ServerURL, agentID), bytes.NewBuffer(body))
	if err != nil {
		return err
	}
	httpReq.Header.Set("Content-Type", "application/json")
	if c.SecretKey != "" {
		httpReq.Header.Set("X-Agent-Secret", c.SecretKey)
	}

	resp, err := c.HTTP.Do(httpReq)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		return fmt.Errorf("server returned status: %d", resp.StatusCode)
	}
	return nil
}
