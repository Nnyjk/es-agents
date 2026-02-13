package app

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"runtime"
	"sync"
	"syscall"
	"time"

	"github.com/easy-station/agent/internal/client"
	"github.com/easy-station/agent/internal/config"
	"github.com/easy-station/agent/internal/plugin"
	"github.com/easy-station/agent/internal/transport"
)

type Agent struct {
	Config        *config.Config
	WSServer      *transport.WSServer
	PluginManager *plugin.Manager

	LogBuffer   []string
	LogBufferMu sync.Mutex
}

func New(cfg *config.Config) *Agent {
	wsServer := transport.NewWSServer(cfg.SecretKey)

	a := &Agent{
		Config:        cfg,
		WSServer:      wsServer,
		PluginManager: plugin.NewManager(),
	}

	wsServer.OnMessage = a.handleMessage

	a.PluginManager.OnOutput = func(line string) {
		a.Log(line)
	}

	return a
}

func (a *Agent) Log(msg string) {
	a.LogBufferMu.Lock()
	defer a.LogBufferMu.Unlock()

	fmt.Println(msg)
	a.LogBuffer = append(a.LogBuffer, msg)
	if len(a.LogBuffer) > 100 {
		a.LogBuffer = a.LogBuffer[1:]
	}

	if a.WSServer.Conn != nil {
		_ = a.WSServer.SendJSON(map[string]interface{}{
			"type":    "LOG",
			"content": msg,
		})
	}
}

func (a *Agent) Run() error {
	a.Log(fmt.Sprintf("Host Agent %s started. Listening on port %d", a.Config.HostID, a.Config.ListenPort))
	go a.heartbeatLoop()
	return a.WSServer.Start(a.Config.ListenPort)
}

func (a *Agent) heartbeatLoop() {
	ticker := time.NewTicker(a.Config.HeartbeatInterval)
	defer ticker.Stop()

	if a.WSServer.Conn != nil {
		_ = a.sendHeartbeat()
	}

	for range ticker.C {
		if a.WSServer.Conn == nil {
			continue
		}
		if err := a.sendHeartbeat(); err != nil {
			fmt.Printf("Heartbeat failed: %v\n", err)
		}
	}
}

func (a *Agent) sendHeartbeat() error {
	req := client.HeartbeatRequest{
		AgentID:   a.Config.HostID,
		Status:    "ONLINE",
		Timestamp: time.Now(),
		Version:   "0.0.1",
		OsType:    getOsType(),
	}
	return a.WSServer.SendJSON(map[string]interface{}{
		"protocolVersion": "2.0",
		"requestId":       fmt.Sprintf("hb-%d", time.Now().UnixNano()),
		"timestamp":       time.Now().UTC().Format(time.RFC3339),
		"type":            "HEARTBEAT",
		"content":         req,
	})
}

func getOsType() string {
	if runtime.GOOS == "linux" {
		if _, err := os.Stat("/.dockerenv"); err == nil {
			return "linux_docker"
		}
	}
	return runtime.GOOS
}

func (a *Agent) handleMessage(msg []byte) {
	var generic struct {
		Type            string          `json:"type"`
		Content         json.RawMessage `json:"content"`
		RequestID       string          `json:"requestId"`
		ProtocolVersion string          `json:"protocolVersion"`
	}

	if err := json.Unmarshal(msg, &generic); err != nil {
		a.Log(fmt.Sprintf("Failed to unmarshal message: %v", err))
		return
	}

	switch generic.Type {
	case "FETCH_LOGS":
		a.handleFetchLogs()
	case "EXEC_CMD":
		var cmdReq struct {
			Command   string `json:"command"`
			TimeoutMs int64  `json:"timeoutMs"`
		}
		if err := json.Unmarshal(generic.Content, &cmdReq); err == nil {
			if cmdReq.TimeoutMs <= 0 {
				cmdReq.TimeoutMs = 30000
			}
			requestID := generic.RequestID
			if requestID == "" {
				requestID = fmt.Sprintf("cmd-%d", time.Now().UnixNano())
			}
			go a.executeCommandWithResult(requestID, cmdReq.Command, time.Duration(cmdReq.TimeoutMs)*time.Millisecond)
		}
	case "INPUT":
		var inputReq struct {
			Content string `json:"content"`
		}
		if err := json.Unmarshal(generic.Content, &inputReq); err == nil {
			if len(inputReq.Content) > 0 {
				a.Log("Interactive shell not available. Please use Command Palette.")
			}
		}
	default:
		a.Log(fmt.Sprintf("Unsupported message type: %s", generic.Type))
	}
}

func (a *Agent) executeCommandWithResult(requestID, command string, timeout time.Duration) {
	started := time.Now().UTC()
	a.Log(fmt.Sprintf("Executing command requestId=%s: %s", requestID, command))

	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	var cmd *exec.Cmd
	if runtime.GOOS == "windows" {
		cmd = exec.CommandContext(ctx, "cmd.exe", "/C", command)
	} else {
		cmd = exec.CommandContext(ctx, "sh", "-c", command)
	}

	output, err := cmd.CombinedOutput()
	for _, line := range bytes.Split(output, []byte("\n")) {
		if len(bytes.TrimSpace(line)) > 0 {
			a.Log(string(line))
		}
	}

	finished := time.Now().UTC()
	exitCode := 0
	if cmd.ProcessState != nil {
		if status, ok := cmd.ProcessState.Sys().(syscall.WaitStatus); ok {
			exitCode = status.ExitStatus()
		}
	}

	outputPreview := string(bytes.TrimSpace(output))
	if len(outputPreview) > 2048 {
		outputPreview = outputPreview[:2048]
	}

	result := map[string]interface{}{
		"status":        "SUCCESS",
		"exitCode":      exitCode,
		"startedAt":     started.Format(time.RFC3339),
		"finishedAt":    finished.Format(time.RFC3339),
		"durationMs":    finished.Sub(started).Milliseconds(),
		"outputPreview": outputPreview,
	}

	if ctx.Err() == context.DeadlineExceeded {
		result["status"] = "TIMEOUT"
		result["exitCode"] = -1
	} else if err != nil {
		result["status"] = "FAILED"
		if exitCode == 0 {
			result["exitCode"] = 1
		}
		result["errorMessage"] = err.Error()
	}

	_ = a.WSServer.SendJSON(map[string]interface{}{
		"protocolVersion": "2.0",
		"requestId":       requestID,
		"timestamp":       time.Now().UTC().Format(time.RFC3339),
		"type":            "EXEC_RESULT",
		"content":         result,
	})
}

func (a *Agent) handleFetchLogs() {
	a.LogBufferMu.Lock()
	logs := make([]string, len(a.LogBuffer))
	copy(logs, a.LogBuffer)
	a.LogBufferMu.Unlock()

	_ = a.WSServer.SendJSON(map[string]interface{}{
		"type":    "LOG_HISTORY",
		"content": logs,
	})
}
