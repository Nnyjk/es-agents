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
	"github.com/easy-station/agent/internal/resource"
	"github.com/easy-station/agent/internal/transport"
	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/disk"
	"github.com/shirou/gopsutil/v3/mem"
)

type Agent struct {
	Config        *config.Config
	WSServer      *transport.WSServer
	PluginManager *plugin.Manager
	Fetcher       *resource.Fetcher
	Packer        *resource.Packer
	Deployer      *resource.Deployer

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
		Fetcher:       resource.NewFetcher(),
		Packer:        resource.NewPacker(),
		Deployer:      resource.NewDeployer(),
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
		CPUUsage:  getCPUUsage(),
		MemUsage:  getMemUsage(),
		DiskUsage: getDiskUsage(),
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
	case "FETCH_RESOURCE":
		var fetchReq resource.FetchRequest
		if err := json.Unmarshal(generic.Content, &fetchReq); err == nil {
			fetchReq.RequestID = generic.RequestID
			if fetchReq.RequestID == "" {
				fetchReq.RequestID = fmt.Sprintf("fetch-%d", time.Now().UnixNano())
			}
			go a.handleFetchResource(fetchReq)
		}
	case "BUILD_PACKAGE":
		var buildReq resource.BuildRequest
		if err := json.Unmarshal(generic.Content, &buildReq); err == nil {
			buildReq.RequestID = generic.RequestID
			if buildReq.RequestID == "" {
				buildReq.RequestID = fmt.Sprintf("build-%d", time.Now().UnixNano())
			}
			go a.handleBuildPackage(buildReq)
		}
	case "DEPLOY":
		var deployReq resource.DeployRequest
		if err := json.Unmarshal(generic.Content, &deployReq); err == nil {
			deployReq.RequestID = generic.RequestID
			if deployReq.RequestID == "" {
				deployReq.RequestID = fmt.Sprintf("deploy-%d", time.Now().UnixNano())
			}
			go a.handleDeploy(deployReq)
		}
	case "HEALTH_CHECK":
		var healthReq resource.HealthCheckRequest
		if err := json.Unmarshal(generic.Content, &healthReq); err == nil {
			healthReq.RequestID = generic.RequestID
			if healthReq.RequestID == "" {
				healthReq.RequestID = fmt.Sprintf("health-%d", time.Now().UnixNano())
			}
			go a.handleHealthCheck(healthReq)
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

// getCPUUsage returns the CPU usage percentage (0-100)
func getCPUUsage() float64 {
	percentages, err := cpu.Percent(time.Second, false)
	if err != nil || len(percentages) == 0 {
		return 0
	}
	return percentages[0]
}

// getMemUsage returns the memory usage percentage (0-100)
func getMemUsage() float64 {
	vmStat, err := mem.VirtualMemory()
	if err != nil {
		return 0
	}
	return vmStat.UsedPercent
}

// getDiskUsage returns the disk usage percentage (0-100) for the root mount
func getDiskUsage() float64 {
	// Get usage for root filesystem
	path := "/"
	if runtime.GOOS == "windows" {
		path = "C:"
	}
	usage, err := disk.Usage(path)
	if err != nil {
		return 0
	}
	return usage.UsedPercent
}

// handleFetchResource handles FETCH_RESOURCE message
func (a *Agent) handleFetchResource(req resource.FetchRequest) {
	a.Log(fmt.Sprintf("Fetching resource requestId=%s type=%s", req.RequestID, req.Source.Type))

	ctx := context.Background()
	result, err := a.Fetcher.Fetch(ctx, req)
	if err != nil {
		a.Log(fmt.Sprintf("Fetch failed requestId=%s: %v", req.RequestID, err))
		result = &resource.FetchResult{
			RequestID: req.RequestID,
			Status:    "FAILED",
			Error:     err.Error(),
			StartedAt: time.Now(),
			FinishedAt: time.Now(),
		}
	}

	if result.Status == "SUCCESS" {
		a.Log(fmt.Sprintf("Fetch succeeded requestId=%s path=%s", req.RequestID, result.Resource.Path))
	} else {
		a.Log(fmt.Sprintf("Fetch failed requestId=%s error=%s", req.RequestID, result.Error))
	}

	a.sendFetchResult(result)
}

// handleBuildPackage handles BUILD_PACKAGE message
func (a *Agent) handleBuildPackage(req resource.BuildRequest) {
	a.Log(fmt.Sprintf("Building package requestId=%s name=%s", req.RequestID, req.Config.Name))

	ctx := context.Background()
	result, err := a.Packer.Build(ctx, req)
	if err != nil {
		a.Log(fmt.Sprintf("Build failed requestId=%s: %v", req.RequestID, err))
		result = &resource.BuildResult{
			RequestID: req.RequestID,
			Status:    "FAILED",
			Error:     err.Error(),
			StartedAt: time.Now(),
			FinishedAt: time.Now(),
		}
	}

	if result.Status == "SUCCESS" {
		a.Log(fmt.Sprintf("Build succeeded requestId=%s path=%s", req.RequestID, result.Package.Path))
	} else {
		a.Log(fmt.Sprintf("Build failed requestId=%s error=%s", req.RequestID, result.Error))
	}

	a.sendBuildResult(result)
}

// handleDeploy handles DEPLOY message
func (a *Agent) handleDeploy(req resource.DeployRequest) {
	a.Log(fmt.Sprintf("Deploying requestId=%s targets=%v", req.RequestID, req.Config.TargetHosts))

	ctx := context.Background()
	result, err := a.Deployer.Deploy(ctx, req)
	if err != nil {
		a.Log(fmt.Sprintf("Deploy failed requestId=%s: %v", req.RequestID, err))
		result = &resource.DeployResult{
			RequestID: req.RequestID,
			Status:    "FAILED",
			Error:     err.Error(),
			StartedAt: time.Now(),
		}
	}

	if result.Status == "SUCCESS" {
		a.Log(fmt.Sprintf("Deploy succeeded requestId=%s", req.RequestID))
	} else {
		a.Log(fmt.Sprintf("Deploy failed requestId=%s error=%s", req.RequestID, result.Error))
	}

	a.sendDeployResult(result)
}

// handleHealthCheck handles HEALTH_CHECK message
func (a *Agent) handleHealthCheck(req resource.HealthCheckRequest) {
	a.Log(fmt.Sprintf("Health check requestId=%s type=%s", req.RequestID, req.Config.Type))

	ctx := context.Background()
	response, err := a.Deployer.HealthCheck(ctx, req)
	if err != nil {
		a.Log(fmt.Sprintf("Health check failed requestId=%s: %v", req.RequestID, err))
		response = &resource.HealthCheckResponse{
			RequestID: req.RequestID,
			Status:    "FAILED",
			Error:     err.Error(),
			CheckTime: time.Now(),
		}
	}

	if response.Status == "SUCCESS" {
		a.Log(fmt.Sprintf("Health check passed requestId=%s", req.RequestID))
	} else {
		a.Log(fmt.Sprintf("Health check failed requestId=%s error=%s", req.RequestID, response.Error))
	}

	a.sendHealthCheckResult(response)
}

// sendFetchResult sends fetch result response
func (a *Agent) sendFetchResult(result *resource.FetchResult) {
	_ = a.WSServer.SendJSON(map[string]interface{}{
		"protocolVersion": "2.0",
		"requestId":       result.RequestID,
		"timestamp":       time.Now().UTC().Format(time.RFC3339),
		"type":            "FETCH_RESULT",
		"content":         result,
	})
}

// sendBuildResult sends build result response
func (a *Agent) sendBuildResult(result *resource.BuildResult) {
	_ = a.WSServer.SendJSON(map[string]interface{}{
		"protocolVersion": "2.0",
		"requestId":       result.RequestID,
		"timestamp":       time.Now().UTC().Format(time.RFC3339),
		"type":            "BUILD_RESULT",
		"content":         result,
	})
}

// sendDeployResult sends deploy result response
func (a *Agent) sendDeployResult(result *resource.DeployResult) {
	_ = a.WSServer.SendJSON(map[string]interface{}{
		"protocolVersion": "2.0",
		"requestId":       result.RequestID,
		"timestamp":       time.Now().UTC().Format(time.RFC3339),
		"type":            "DEPLOY_RESULT",
		"content":         result,
	})
}

// sendHealthCheckResult sends health check result response
func (a *Agent) sendHealthCheckResult(response *resource.HealthCheckResponse) {
	_ = a.WSServer.SendJSON(map[string]interface{}{
		"protocolVersion": "2.0",
		"requestId":       response.RequestID,
		"timestamp":       time.Now().UTC().Format(time.RFC3339),
		"type":            "HEALTH_CHECK_RESULT",
		"content":         response,
	})
}
