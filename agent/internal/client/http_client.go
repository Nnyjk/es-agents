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
	AgentID   string    `json:"agentId"`
	Status    string    `json:"status"`
	Timestamp time.Time `json:"timestamp"`
	Version   string    `json:"version"`
	OsType    string    `json:"osType"`
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
