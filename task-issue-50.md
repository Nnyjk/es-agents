# Issue #50: Agent 任务调度与结果聚合机制

## 目标
实现 Agent 端 Plugin 任务分发和结果聚合机制，支持 Server 下发任务到 Host Agent，Host Agent 分发到 Plugin 执行并聚合结果回传。

## 实现内容

### 1. Agent 端 Plugin 任务分发 (`agent/internal/app/agent.go`)
添加 `EXEC_PLUGIN_TASK` 消息类型处理：
- 解析任务请求（taskId, pluginId, taskType, parameters, timeoutMs）
- 调用 PluginManager.ExecutePlugin 执行
- 异步执行并回传结果到 Server

### 2. Plugin Manager 执行方法 (`agent/internal/plugin/manager.go`)
添加 `ExecutePlugin` 方法：
- 根据 pluginId 查找插件
- 构建插件命令（通过 stdio 与插件通信）
- 执行并收集输出
- 支持超时控制
- 返回 PluginTaskResult

### 3. Plugin 任务结果类型 (`agent/internal/plugin/types.go`)
添加 `PluginTaskResult` 结构体：
```go
type PluginTaskResult struct {
    TaskID     string                 `json:"taskId"`
    PluginID   string                 `json:"pluginId"`
    Status     string                 `json:"status"` // SUCCESS/FAILED/TIMEOUT
    ExitCode   int                    `json:"exitCode,omitempty"`
    Output     string                 `json:"output,omitempty"`
    Error      string                 `json:"error,omitempty"`
    DurationMs int64                  `json:"durationMs"`
    Data       map[string]interface{} `json:"data,omitempty"`
}
```

### 4. Agent 端结果聚合 (`agent/internal/app/agent.go`)
添加 `TaskAggregator` 结构体和方法：
- 收集多个 Plugin 执行结果
- 聚合后统一回传 Server
- 支持并发安全（mutex）

## 验收标准
- [ ] Agent 可接收 EXEC_PLUGIN_TASK 消息并分发到 Plugin
- [ ] Plugin 执行结果正确回传
- [ ] 支持超时处理
- [ ] 错误处理完善
- [ ] Go 代码通过 `go build ./...` 编译验证

## 关联
- Closes #50
- 依赖：#42 (已完成)
