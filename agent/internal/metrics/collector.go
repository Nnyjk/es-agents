package metrics

import (
	"context"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/disk"
	"github.com/shirou/gopsutil/v3/mem"
)

var (
	// Agent 运行指标
	AgentStatus = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "esa_agent_status",
		Help: "Agent 运行状态 (1=运行中，0=停止)",
	})

	TaskExecutionsTotal = promauto.NewCounter(prometheus.CounterOpts{
		Name: "esa_agent_task_executions_total",
		Help: "任务执行总次数",
	})

	TaskExecutionDuration = promauto.NewHistogram(prometheus.HistogramOpts{
		Name:    "esa_agent_task_execution_duration_seconds",
		Help:    "任务执行延迟（秒）",
		Buckets: prometheus.DefBuckets,
	})

	// 主机资源指标
	CPUUsagePercent = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "esa_host_cpu_usage_percent",
		Help: "CPU 使用率百分比",
	})

	MemoryUsagePercent = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "esa_host_memory_usage_percent",
		Help: "内存使用率百分比",
	})

	DiskUsagePercent = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "esa_host_disk_usage_percent",
		Help: "磁盘使用率百分比",
	})

	// WebSocket 连接指标
	WebSocketConnections = promauto.NewGauge(prometheus.GaugeOpts{
		Name: "esa_agent_websocket_connections",
		Help: "当前 WebSocket 连接数",
	})

	// 插件任务指标
	PluginTaskSuccessTotal = promauto.NewCounter(prometheus.CounterOpts{
		Name: "esa_agent_plugin_task_success_total",
		Help: "成功执行的插件任务总数",
	})

	PluginTaskFailureTotal = promauto.NewCounter(prometheus.CounterOpts{
		Name: "esa_agent_plugin_task_failure_total",
		Help: "失败的插件任务总数",
	})
)

// Collector 指标采集器
type Collector struct {
	ctx context.Context
}

// NewCollector 创建新的指标采集器
func NewCollector(ctx context.Context) *Collector {
	return &Collector{
		ctx: ctx,
	}
}

// StartCollection 启动指标采集循环
func (c *Collector) StartCollection() {
	ticker := time.NewTicker(15 * time.Second)
	defer ticker.Stop()

	// 设置 Agent 状态为运行中
	AgentStatus.Set(1)

	for {
		select {
		case <-c.ctx.Done():
			AgentStatus.Set(0)
			return
		case <-ticker.C:
			c.collectHostMetrics()
		}
	}
}

// collectHostMetrics 采集主机指标
func (c *Collector) collectHostMetrics() {
	// CPU 使用率
	cpuPercent, err := cpu.Percent(0, false)
	if err == nil && len(cpuPercent) > 0 {
		CPUUsagePercent.Set(cpuPercent[0])
	}

	// 内存使用率
	memInfo, err := mem.VirtualMemory()
	if err == nil {
		MemoryUsagePercent.Set(memInfo.UsedPercent)
	}

	// 磁盘使用率
	diskInfo, err := disk.Usage("/")
	if err == nil {
		DiskUsagePercent.Set(diskInfo.UsedPercent)
	}
}

// RecordTaskExecution 记录任务执行
func RecordTaskExecution(durationSeconds float64, success bool) {
	TaskExecutionsTotal.Inc()
	TaskExecutionDuration.Observe(durationSeconds)

	if success {
		PluginTaskSuccessTotal.Inc()
	} else {
		PluginTaskFailureTotal.Inc()
	}
}

// SetWebSocketConnected 设置 WebSocket 连接状态
func SetWebSocketConnected(connected bool) {
	if connected {
		WebSocketConnections.Set(1)
	} else {
		WebSocketConnections.Set(0)
	}
}
