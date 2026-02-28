package app

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"os/exec"
	"runtime"
	"strings"
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
	
	LogFile     *os.File
	LogFileMu   sync.Mutex
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

// initLogFile initializes the log file for persistent logging
func (a *Agent) initLogFile() error {
	a.LogFileMu.Lock()
	defer a.LogFileMu.Unlock()
	
	// Open log file in append mode, create if not exists
	logFile, err := os.OpenFile("host-agent.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
	if err != nil {
		return fmt.Errorf("failed to open log file: %w", err)
	}
	a.LogFile = logFile
	return nil
}

// Log writes a message to console, log buffer, log file, and WebSocket
func (a *Agent) Log(msg string) {
	timestamp := time.Now().Format("2006-01-02 15:04:05")
	logLine := fmt.Sprintf("[%s] %s", timestamp, msg)
	
	// Write to console
	fmt.Println(logLine)
	
	// Write to buffer (for in-memory recent logs)
	a.LogBufferMu.Lock()
	a.LogBuffer = append(a.LogBuffer, logLine)
	if len(a.LogBuffer) > 100 {
		a.LogBuffer = a.LogBuffer[1:]
	}
	a.LogBufferMu.Unlock()
	
	// Write to log file
	a.LogFileMu.Lock()
	if a.LogFile != nil {
		a.LogFile.WriteString(logLine + "\n")
	}
	a.LogFileMu.Unlock()
	
	// Send to WebSocket (without timestamp to keep original format)
	if a.WSServer.Conn != nil {
		_ = a.WSServer.SendJSON(map[string]interface{}{
			"type":    "LOG",
			"content": msg,
		})
	}
}

func (a *Agent) Run() error {
	// Initialize log file
	if err := a.initLogFile(); err != nil {
		fmt.Printf("Warning: failed to initialize log file: %v\n", err)
	}
	
	a.Log(fmt.Sprintf("Host Agent %s started. Listening on port %d", a.Config.HostID, a.Config.ListenPort))
	go a.heartbeatLoop()
	return a.WSServer.Start(a.Config.ListenPort)
}

// Close cleans up resources
func (a *Agent) Close() {
	a.LogFileMu.Lock()
	defer a.LogFileMu.Unlock()
	if a.LogFile != nil {
		a.LogFile.Close()
	}
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
			TimeoutMs *int64 `json:"timeoutMs"`
		}
		if err := json.Unmarshal(generic.Content, &cmdReq); err == nil {
			requestID := generic.RequestID
			if requestID == "" {
				requestID = fmt.Sprintf("cmd-%d", time.Now().UnixNano())
			}
			var timeout time.Duration
			if cmdReq.TimeoutMs != nil && *cmdReq.TimeoutMs > 0 {
				timeout = time.Duration(*cmdReq.TimeoutMs) * time.Millisecond
			}
			go a.executeCommandWithResult(requestID, cmdReq.Command, timeout)
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

	ctx := context.Background()
	cancel := func() {}
	if timeout > 0 {
		ctx, cancel = context.WithTimeout(context.Background(), timeout)
	}
	defer cancel()

	var cmd *exec.Cmd
	if runtime.GOOS == "windows" {
		if timeout > 0 {
			cmd = exec.CommandContext(ctx, "cmd.exe", "/C", command)
		} else {
			cmd = exec.Command("cmd.exe", "/C", command)
		}
	} else {
		if timeout > 0 {
			cmd = exec.CommandContext(ctx, "sh", "-c", command)
		} else {
			cmd = exec.Command("sh", "-c", command)
		}
	}

	stdout, err := cmd.StdoutPipe()
	if err != nil {
		a.sendExecResult(requestID, started, "FAILED", 1, "", err.Error())
		return
	}
	stderr, err := cmd.StderrPipe()
	if err != nil {
		a.sendExecResult(requestID, started, "FAILED", 1, "", err.Error())
		return
	}

	if err := cmd.Start(); err != nil {
		a.sendExecResult(requestID, started, "FAILED", 1, "", err.Error())
		return
	}

	const maxPreview = 2048
	var previewMu sync.Mutex
	preview := strings.Builder{}
	appendPreview := func(s string) {
		previewMu.Lock()
		defer previewMu.Unlock()
		remaining := maxPreview - preview.Len()
		if remaining <= 0 {
			return
		}
		if len(s) > remaining {
			preview.WriteString(s[:remaining])
			return
		}
		preview.WriteString(s)
	}

	stream := func(r io.Reader) {
		scanner := bufio.NewScanner(r)
		scanner.Buffer(make([]byte, 0, 64*1024), 1024*1024)
		for scanner.Scan() {
			line := scanner.Text()
			a.Log(line)
			appendPreview(line + "\n")
		}
	}

	var wg sync.WaitGroup
	wg.Add(2)
	go func() {
		defer wg.Done()
		stream(stdout)
	}()
	go func() {
		defer wg.Done()
		stream(stderr)
	}()

	err = cmd.Wait()
	wg.Wait()

	exitCode := 0
	if cmd.ProcessState != nil {
		if status, ok := cmd.ProcessState.Sys().(syscall.WaitStatus); ok {
			exitCode = status.ExitStatus()
		}
	}

	status := "SUCCESS"
	errorMessage := ""
	if timeout > 0 && ctx.Err() == context.DeadlineExceeded {
		status = "TIMEOUT"
		exitCode = -1
		errorMessage = "command timed out"
	} else if err != nil {
		status = "FAILED"
		if exitCode == 0 {
			exitCode = 1
		}
		errorMessage = err.Error()
	}

	a.sendExecResult(requestID, started, status, exitCode, strings.TrimSpace(preview.String()), errorMessage)
}

func (a *Agent) sendExecResult(requestID string, started time.Time, status string, exitCode int, outputPreview string, errorMessage string) {
	finished := time.Now().UTC()
	result := map[string]interface{}{
		"status":        status,
		"exitCode":      exitCode,
		"startedAt":     started.Format(time.RFC3339),
		"finishedAt":    finished.Format(time.RFC3339),
		"durationMs":    finished.Sub(started).Milliseconds(),
		"outputPreview": outputPreview,
	}
	if errorMessage != "" {
		result["errorMessage"] = errorMessage
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
