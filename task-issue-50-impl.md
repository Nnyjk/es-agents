## 任务：实现 Issue #50 - Agent 任务调度与结果聚合机制

## 需求
实现 Agent 端 Plugin 任务分发和结果聚合机制：

1. **Agent 端 Plugin 任务分发** (`agent/internal/app/agent.go`)
   - 添加 EXEC_PLUGIN_TASK 消息类型处理
   - 根据 pluginId 和 taskType 路由到对应 Plugin
   - 异步执行并回传结果

2. **Plugin Manager 执行方法** (`agent/internal/plugin/manager.go`)
   - 添加 ExecutePlugin 方法
   - 支持超时控制
   - 返回 PluginTaskResult

3. **Plugin 任务结果类型** (`agent/internal/plugin/types.go`)
   - 添加 PluginTaskResult 结构体
   - 包含 taskId, pluginId, status, exitCode, output, error, durationMs, data 字段

4. **Agent 端结果聚合** (`agent/internal/app/agent.go`)
   - 添加 TaskAggregator 结构体
   - 实现结果聚合逻辑
   - 发送聚合结果到 Server

## 验收标准
- Agent 可接收 EXEC_PLUGIN_TASK 消息并分发到 Plugin
- Plugin 执行结果正确回传
- 支持超时处理
- 错误处理完善

## 注意
- 保持与现有代码风格一致
- 添加适当的日志输出
- 确保并发安全（使用 mutex）
- Go 代码需通过 `go build` 编译验证

请逐步实现上述功能，每完成一步进行编译验证。
