package plugin

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"sync"
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
}

func NewManager() *Manager {
	return &Manager{
		agents: make(map[string]*Agent),
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
	} else {
		fmt.Printf("Agent %s exited successfully\n", id)
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
