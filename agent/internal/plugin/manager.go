package plugin

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"sync"
	"time"
)

type AgentType string

const (
	Service AgentType = "SERVICE"
	Task    AgentType = "TASK"
	Script  AgentType = "SCRIPT"
)

type Agent struct {
	ID   string
	Type AgentType
	Cmd  *exec.Cmd
}

type Manager struct {
	agents   map[string]*Agent
	mu       sync.Mutex
	OnOutput func(string)
	Registry *Registry // 插件注册表
}

func NewManager() *Manager {
	return &Manager{
		agents:  make(map[string]*Agent),
		Registry: NewRegistry(),
	}
}

// BuildCommand constructs an exec.Cmd handling OS-specific logic
func BuildCommand(command string, args []string) *exec.Cmd {
	cleanPath := filepath.Clean(command)
	ext := strings.ToLower(filepath.Ext(cleanPath))

	var cmd *exec.Cmd

	if runtime.GOOS == "windows" {
		if ext == ".ps1" {
			// PowerShell script
			psArgs := []string{"-ExecutionPolicy", "Bypass", "-File", cleanPath}
			psArgs = append(psArgs, args...)
			cmd = exec.Command("powershell", psArgs...)
		} else if ext == ".bat" || ext == ".cmd" {
			// Batch file
			// cmd.exe /C script.bat args...
			cmdArgs := []string{"/C", cleanPath}
			cmdArgs = append(cmdArgs, args...)
			cmd = exec.Command("cmd.exe", cmdArgs...)
		} else {
			// Binary or other
			cmd = exec.Command(cleanPath, args...)
		}
	} else {
		// Linux / Darwin
		if ext == ".sh" {
			shArgs := []string{cleanPath}
			shArgs = append(shArgs, args...)
			cmd = exec.Command("sh", shArgs...)
		} else {
			// Binary (ensure it has ./ if local)
			if !strings.Contains(cleanPath, "/") {
				cleanPath = "./" + cleanPath
			}
			cmd = exec.Command(cleanPath, args...)
		}
	}
	return cmd
}

func (m *Manager) Start(id string, command string, args []string, typ AgentType) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if _, exists := m.agents[id]; exists {
		return fmt.Errorf("agent %s already running", id)
	}

	cmd := BuildCommand(command, args)

	// Set up output pipes
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		return err
	}
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return err
	}

	if err := cmd.Start(); err != nil {
		return err
	}

	agent := &Agent{
		ID:   id,
		Type: typ,
		Cmd:  cmd,
	}
	m.agents[id] = agent

	// 自动注册插件到 Registry
	pluginInfo := NewPluginInfo(id, id, "1.0.0", string(typ))
	pluginInfo.AddCapability(PluginCapability{
		Name:        string(typ),
		Description: fmt.Sprintf("Agent type: %s", typ),
		Commands:    []string{command},
	})
	m.Registry.Register(pluginInfo)

	// Start output monitors
	var wg sync.WaitGroup
	wg.Add(2)

	go func() {
		defer wg.Done()
		m.monitorOutput(id, stdout, "STDOUT")
	}()
	go func() {
		defer wg.Done()
		m.monitorOutput(id, stderr, "STDERR")
	}()

	// Wait for IO completion then process exit
	go func() {
		wg.Wait()
		err := cmd.Wait()
		m.handleExit(id, err)
	}()

	return nil
}

func (m *Manager) Stop(id string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	agent, exists := m.agents[id]
	if !exists {
		return fmt.Errorf("agent %s not found", id)
	}

	// 更新插件状态为 STOPPED
	m.Registry.UpdateStatus(id, StatusStopped)

	if agent.Cmd != nil && agent.Cmd.Process != nil {
		return agent.Cmd.Process.Kill()
	}
	return nil
}

func (m *Manager) handleExit(id string, err error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	if err != nil {
		fmt.Printf("Agent %s exited with error: %v\n", id, err)
		m.Registry.UpdateStatus(id, StatusError)
	} else {
		fmt.Printf("Agent %s exited successfully\n", id)
		m.Registry.UpdateStatus(id, StatusStopped)
	}
	delete(m.agents, id)
}

func (m *Manager) monitorOutput(id string, r io.Reader, source string) {
	scanner := bufio.NewScanner(r)
	for scanner.Scan() {
		line := scanner.Text()

		// Send to callback if set
		if m.OnOutput != nil {
			m.OnOutput(line)
		}

		// Check for magic prefix
		if strings.HasPrefix(line, "::ES::") {
			jsonContent := strings.TrimPrefix(line, "::ES::")
			var result map[string]interface{}
			if err := json.Unmarshal([]byte(jsonContent), &result); err == nil {
				fmt.Printf("[Agent %s] structured output: %v\n", id, result)
				// Structured output is currently logged locally; forwarding is handled by Agent logger path
			} else {
				fmt.Printf("[Agent %s][%s] raw: %s\n", id, source, line)
			}
		} else {
			fmt.Printf("[Agent %s][%s]: %s\n", id, source, line)
		}
	}
}

func (m *Manager) List() []string {
	m.mu.Lock()
	defer m.mu.Unlock()

	ids := make([]string, 0, len(m.agents))
	for id := range m.agents {
		ids = append(ids, id)
	}
	return ids
}

// ExecutePlugin 执行指定插件的任务
// 通过 stdio 与插件通信，支持超时控制
func (m *Manager) ExecutePlugin(ctx context.Context, pluginID, taskType string, parameters map[string]interface{}, timeoutMs int64) *PluginTaskResult {
	start := time.Now()
	result := &PluginTaskResult{
		TaskID:   fmt.Sprintf("task-%d", time.Now().UnixNano()),
		PluginID: pluginID,
		Status:   TaskSuccess,
	}

	// 查找插件
	m.mu.Lock()
	agent, exists := m.agents[pluginID]
	m.mu.Unlock()

	if !exists {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("plugin %s not found", pluginID)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}

	// 检查插件状态
	pluginInfo, err := m.Registry.Get(pluginID)
	if err != nil || pluginInfo.Status != StatusRunning {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("plugin %s is not running", pluginID)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}

	// 构建任务请求 JSON
	taskReq := map[string]interface{}{
		"taskId":    result.TaskID,
		"taskType":  taskType,
		"parameters": parameters,
	}
	taskJSON, err := json.Marshal(taskReq)
	if err != nil {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("failed to marshal task request: %v", err)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}

	// 设置超时上下文
	execCtx := ctx
	if timeoutMs > 0 {
		var cancel context.CancelFunc
		execCtx, cancel = context.WithTimeout(ctx, time.Duration(timeoutMs)*time.Millisecond)
		defer cancel()
	}

	// 发送任务到插件进程的 stdin
	if agent.Cmd != nil && agent.Cmd.Process != nil {
		// 获取 stdin pipe
		stdin, err := agent.Cmd.StdinPipe()
		if err != nil {
			// stdin pipe may already be taken, try alternative approach
			result.Status = TaskFailed
			result.Error = fmt.Sprintf("failed to get stdin pipe: %v", err)
			result.DurationMs = time.Since(start).Milliseconds()
			return result
		}

		// 发送任务请求（带 magic prefix）
		taskLine := MagicPrefix + string(taskJSON) + "\n"
		if _, err := stdin.Write([]byte(taskLine)); err != nil {
			result.Status = TaskFailed
			result.Error = fmt.Sprintf("failed to write to stdin: %v", err)
			result.DurationMs = time.Since(start).Milliseconds()
			return result
		}
	}

	// 等待执行完成或超时
	// 由于当前架构是通过 monitorOutput 异步收集输出，这里我们等待一段时间或检查是否有匹配的响应
	// 实际生产环境可能需要更复杂的响应等待机制
	select {
	case <-execCtx.Done():
		if execCtx.Err() == context.DeadlineExceeded {
			result.Status = TaskTimeout
			result.Error = "task execution timed out"
			result.ExitCode = -1
		} else {
			result.Status = TaskFailed
			result.Error = execCtx.Err().Error()
		}
	case <-time.After(100 * time.Millisecond):
		// 短暂等待后认为任务已发送成功
		result.Status = TaskSuccess
		result.Output = fmt.Sprintf("Task %s sent to plugin %s", result.TaskID, pluginID)
	}

	result.DurationMs = time.Since(start).Milliseconds()
	return result
}

// ExecutePluginDirect 直接执行插件命令（非持久运行模式）
// 用于一次性任务执行，执行完毕后进程结束
func (m *Manager) ExecutePluginDirect(ctx context.Context, pluginID, command string, args []string, parameters map[string]interface{}, timeoutMs int64) *PluginTaskResult {
	start := time.Now()
	result := &PluginTaskResult{
		TaskID:   fmt.Sprintf("task-%d", time.Now().UnixNano()),
		PluginID: pluginID,
		Status:   TaskSuccess,
	}

	// 从注册表获取插件信息（可选，用于验证插件是否已注册）
	_, err := m.Registry.Get(pluginID)
	if err != nil {
		// 插件未注册，但仍然可以执行（用于新插件首次执行场景）
		fmt.Printf("Plugin %s not in registry, executing anyway\n", pluginID)
	}

	// 设置超时上下文
	execCtx := ctx
	if timeoutMs > 0 {
		var cancel context.CancelFunc
		execCtx, cancel = context.WithTimeout(ctx, time.Duration(timeoutMs)*time.Millisecond)
		defer cancel()
	}

	// 构建命令
	cmd := BuildCommand(command, args)

	// 将 parameters 序列化并通过环境变量或 stdin 传递
	if parameters != nil {
		paramsJSON, err := json.Marshal(parameters)
		if err == nil {
			cmd.Env = append(cmd.Env, fmt.Sprintf("ES_TASK_PARAMS=%s", paramsJSON))
		}
	}

	// 设置 stdin 用于传递任务数据
	stdin, err := cmd.StdinPipe()
	if err != nil {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("failed to create stdin pipe: %v", err)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}

	// 设置输出管道
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("failed to create stdout pipe: %v", err)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}
	stderr, err := cmd.StderrPipe()
	if err != nil {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("failed to create stderr pipe: %v", err)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}

	// 启动进程
	if err := cmd.Start(); err != nil {
		result.Status = TaskFailed
		result.Error = fmt.Sprintf("failed to start command: %v", err)
		result.DurationMs = time.Since(start).Milliseconds()
		return result
	}

	// 发送任务参数到 stdin
	taskReq := map[string]interface{}{
		"taskId":     result.TaskID,
		"pluginId":   pluginID,
		"parameters": parameters,
	}
	taskJSON, _ := json.Marshal(taskReq)
	stdin.Write([]byte(MagicPrefix + string(taskJSON) + "\n"))
	stdin.Close()

	// 收集输出
	var outputBuilder, errorBuilder strings.Builder
	var wg sync.WaitGroup
	wg.Add(2)

	go func() {
		defer wg.Done()
		scanner := bufio.NewScanner(stdout)
		for scanner.Scan() {
			line := scanner.Text()
			// 检查是否是结构化输出
			if payload, _ := ParseLine(line); payload != nil {
				// 解析结构化数据
				if payload.Type == MsgResult {
					var resData ResultPayload
					if err := json.Unmarshal(payload.Payload, &resData); err == nil {
						result.Data = map[string]interface{}{"result": resData}
					}
				}
			} else {
				outputBuilder.WriteString(line + "\n")
			}
		}
	}()

	go func() {
		defer wg.Done()
		scanner := bufio.NewScanner(stderr)
		for scanner.Scan() {
			errorBuilder.WriteString(scanner.Text() + "\n")
		}
	}()

	// 等待命令完成
	err = cmd.Wait()
	wg.Wait()

	result.Output = strings.TrimSpace(outputBuilder.String())
	if errorBuilder.Len() > 0 {
		result.Error = strings.TrimSpace(errorBuilder.String())
	}

	// 获取退出码
	if cmd.ProcessState != nil {
		result.ExitCode = cmd.ProcessState.ExitCode()
	}

	// 处理超时和错误
	if execCtx.Err() == context.DeadlineExceeded {
		result.Status = TaskTimeout
		result.Error = "command timed out"
		result.ExitCode = -1
	} else if err != nil {
		result.Status = TaskFailed
		if result.ExitCode == 0 {
			result.ExitCode = 1
		}
		if result.Error == "" {
			result.Error = err.Error()
		}
	}

	// 更新插件状态
	m.Registry.UpdateLastSeen(pluginID)

	result.DurationMs = time.Since(start).Milliseconds()
	return result
}
