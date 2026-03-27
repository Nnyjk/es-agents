package plugin

import (
	"errors"
	"sync"
	"time"
)

// Registry 插件注册表
type Registry struct {
	plugins map[string]*PluginInfo
	mu      sync.RWMutex
}

// NewRegistry 创建新的插件注册表
func NewRegistry() *Registry {
	return &Registry{
		plugins: make(map[string]*PluginInfo),
	}
}

// Register 注册插件
func (r *Registry) Register(info *PluginInfo) error {
	if info == nil {
		return errors.New("plugin info cannot be nil")
	}
	if info.ID == "" {
		return errors.New("plugin id cannot be empty")
	}

	r.mu.Lock()
	defer r.mu.Unlock()

	if _, exists := r.plugins[info.ID]; exists {
		return errors.New("plugin already registered: " + info.ID)
	}

	r.plugins[info.ID] = info
	return nil
}

// Unregister 注销插件
func (r *Registry) Unregister(id string) error {
	r.mu.Lock()
	defer r.mu.Unlock()

	if _, exists := r.plugins[id]; !exists {
		return errors.New("plugin not found: " + id)
	}

	delete(r.plugins, id)
	return nil
}

// List 获取所有插件列表
func (r *Registry) List() []PluginInfo {
	r.mu.RLock()
	defer r.mu.RUnlock()

	list := make([]PluginInfo, 0, len(r.plugins))
	for _, info := range r.plugins {
		list = append(list, *info)
	}
	return list
}

// Get 获取插件详情
func (r *Registry) Get(id string) (*PluginInfo, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()

	info, exists := r.plugins[id]
	if !exists {
		return nil, errors.New("plugin not found: " + id)
	}

	return info, nil
}

// UpdateStatus 更新插件状态
func (r *Registry) UpdateStatus(id string, status PluginStatus) error {
	r.mu.Lock()
	defer r.mu.Unlock()

	info, exists := r.plugins[id]
	if !exists {
		return errors.New("plugin not found: " + id)
	}

	info.Status = status
	info.LastSeen = time.Now()
	return nil
}

// HealthCheck 健康检查
// 检查插件最后活跃时间，如果超过阈值则标记为 ERROR
func (r *Registry) HealthCheck(id string, timeout time.Duration) error {
	r.mu.Lock()
	defer r.mu.Unlock()

	info, exists := r.plugins[id]
	if !exists {
		return errors.New("plugin not found: " + id)
	}

	if time.Since(info.LastSeen) > timeout {
		info.Status = StatusError
		return errors.New("plugin health check failed: timeout")
	}

	return nil
}

// CheckAll 对所有插件进行健康检查
func (r *Registry) CheckAll(timeout time.Duration) []string {
	r.mu.Lock()
	defer r.mu.Unlock()

	var unhealthy []string
	for id, info := range r.plugins {
		if time.Since(info.LastSeen) > timeout {
			info.Status = StatusError
			unhealthy = append(unhealthy, id)
		}
	}
	return unhealthy
}

// Count 获取插件数量
func (r *Registry) Count() int {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return len(r.plugins)
}

// Clear 清空注册表
func (r *Registry) Clear() {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.plugins = make(map[string]*PluginInfo)
}