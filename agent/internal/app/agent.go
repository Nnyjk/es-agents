package app

import (
	"encoding/json"
	"fmt"
	"os"
	"runtime"
	"sync"
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

	// Hook up plugin output to logger
	a.PluginManager.OnOutput = func(line string) {
		a.Log(line)
	}

	return a
}

func (a *Agent) Log(msg string) {
	a.LogBufferMu.Lock()
	defer a.LogBufferMu.Unlock()

	// Print to stdout for debugging
	fmt.Println(msg)

	// Add to buffer
	a.LogBuffer = append(a.LogBuffer, msg)
	if len(a.LogBuffer) > 100 {
		a.LogBuffer = a.LogBuffer[1:]
	}

	// Send to WebSocket
	if a.WSServer.Conn != nil {
		out := map[string]interface{}{
			"type":    "LOG",
			"content": msg,
		}
		a.WSServer.SendJSON(out)
	}
}

func (a *Agent) Run() error {
	a.Log(fmt.Sprintf("Host Agent %s started. Listening on port %d", a.Config.HostID, a.Config.ListenPort))

	// Start Heartbeat Loop
	go a.heartbeatLoop()

	// Start Server (Blocking)
	return a.WSServer.Start(a.Config.ListenPort)
}

func (a *Agent) heartbeatLoop() {
	ticker := time.NewTicker(a.Config.HeartbeatInterval)
	defer ticker.Stop()

	// Immediate first heartbeat (might fail if not connected yet)
	if a.WSServer.Conn != nil {
		a.sendHeartbeat()
	}

	for range ticker.C {
		if a.WSServer.Conn == nil {
			continue
		}
		if err := a.sendHeartbeat(); err != nil {
			fmt.Printf("Heartbeat failed: %v\n", err)
		} else {
			// fmt.Println("Heartbeat sent")
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
	// Wrap in JSON with type="HEARTBEAT" for WebSocket
	msg := map[string]interface{}{
		"type":    "HEARTBEAT",
		"content": req,
	}
	return a.WSServer.SendJSON(msg)
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
	// Parse generic message first
	var generic struct {
		Type    string          `json:"type"`
		Content json.RawMessage `json:"content"`
		// Legacy fields support
		CommandName string `json:"commandName"`
		ID          string `json:"id"`
	}

	if err := json.Unmarshal(msg, &generic); err != nil {
		a.Log(fmt.Sprintf("Failed to unmarshal message: %v", err))
		return
	}

	// Handle Protocol Types
	switch generic.Type {
	case "FETCH_LOGS":
		a.handleFetchLogs()
	case "EXEC_CMD":
		var cmdReq struct {
			Command string `json:"command"`
		}
		if err := json.Unmarshal(generic.Content, &cmdReq); err == nil {
			a.Log(fmt.Sprintf("Executing command: %s", cmdReq.Command))

			var cmdName string
			var cmdArgs []string
			if runtime.GOOS == "windows" {
				cmdName = "cmd.exe"
				cmdArgs = []string{"/C", cmdReq.Command}
			} else {
				cmdName = "sh"
				cmdArgs = []string{"-c", cmdReq.Command}
			}

			// Simple execution
			go a.PluginManager.Start("cmd-"+time.Now().Format("150405"), cmdName, cmdArgs, plugin.Task)
		}
	case "INPUT":
		// Simple Line-Buffered Input for non-PTY shell
		var inputReq struct {
			Content string `json:"content"`
		}
		if err := json.Unmarshal(generic.Content, &inputReq); err == nil {
			if len(inputReq.Content) > 0 {
				a.Log("Interactive shell not available. Please use Command Palette.")
			}
		}
	default:
		// Legacy support (AgentTask)
		if generic.CommandName != "" {
			var task client.AgentTask
			json.Unmarshal(msg, &task) // Re-unmarshal to full struct
			go a.executeTask(task)
		}
	}
}

func (a *Agent) handleFetchLogs() {
	a.LogBufferMu.Lock()
	logs := make([]string, len(a.LogBuffer))
	copy(logs, a.LogBuffer)
	a.LogBufferMu.Unlock()

	resp := map[string]interface{}{
		"type":    "LOG_HISTORY",
		"content": logs,
	}
	a.WSServer.SendJSON(resp)
}

func (a *Agent) executeTask(task client.AgentTask) {
	fmt.Printf("Executing task %s with args: %s\n", task.ID, task.Args)

	var err error
	if task.CommandName == "start-agent" {
		// Real implementation would parse task.Args
		err = a.PluginManager.Start(task.ID, "echo", []string{"Agent Started"}, plugin.Task)
	} else if task.CommandName == "stop-agent" {
		err = a.PluginManager.Stop(task.ID)
	} else {
		fmt.Printf("Generic command execution for %s\n", task.CommandName)
	}

	if err != nil {
		fmt.Printf("Task execution error: %v\n", err)
	}
}
