package plugin

import "time"

// PluginStatus 插件状态
type PluginStatus string

const (
	StatusRunning PluginStatus = "RUNNING"
	StatusStopped PluginStatus = "STOPPED"
	StatusError   PluginStatus = "ERROR"
)

// PluginCapability 插件能力描述
type PluginCapability struct {
	Name        string   `json:"name"`
	Description string   `json:"description"`
	Commands    []string `json:"commands,omitempty"`
}

// PluginInfo 插件信息
type PluginInfo struct {
	ID           string             `json:"id"`
	Name         string             `json:"name"`
	Version      string             `json:"version"`
	Description  string             `json:"description"`
	Capabilities []PluginCapability `json:"capabilities"`
	Status       PluginStatus       `json:"status"`
	RegisteredAt time.Time          `json:"registeredAt"`
	LastSeen     time.Time          `json:"lastSeen"`
}

// NewPluginInfo 创建新的插件信息
func NewPluginInfo(id, name, version, description string) *PluginInfo {
	now := time.Now()
	return &PluginInfo{
		ID:           id,
		Name:         name,
		Version:      version,
		Description:  description,
		Capabilities: []PluginCapability{},
		Status:       StatusRunning,
		RegisteredAt: now,
		LastSeen:     now,
	}
}

// AddCapability 添加能力
func (p *PluginInfo) AddCapability(cap PluginCapability) {
	p.Capabilities = append(p.Capabilities, cap)
}

// UpdateLastSeen 更新最后活跃时间
func (p *PluginInfo) UpdateLastSeen() {
	p.LastSeen = time.Now()
}

// SetStatus 设置状态
func (p *PluginInfo) SetStatus(status PluginStatus) {
	p.Status = status
	p.UpdateLastSeen()
}

// PluginTaskStatus 任务执行状态
type PluginTaskStatus string

const (
	TaskSuccess PluginTaskStatus = "SUCCESS"
	TaskFailed  PluginTaskStatus = "FAILED"
	TaskTimeout PluginTaskStatus = "TIMEOUT"
)

// PluginTaskResult 插件任务执行结果
type PluginTaskResult struct {
	TaskID     string                 `json:"taskId"`
	PluginID   string                 `json:"pluginId"`
	Status     PluginTaskStatus       `json:"status"`
	ExitCode   int                    `json:"exitCode,omitempty"`
	Output     string                 `json:"output,omitempty"`
	Error      string                 `json:"error,omitempty"`
	DurationMs int64                  `json:"durationMs"`
	Data       map[string]interface{} `json:"data,omitempty"`
}