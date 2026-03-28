# Issue #116 Agent 自动升级能力 - 实现计划

## 需求分析

实现 Agent 远程自动升级能力：
1. 服务端下发升级指令与新版本包
2. Host Agent 自动下载、校验、升级自身与子插件
3. 升级过程无服务中断，失败自动回滚
4. 升级进度与结果上报服务端

## 当前状态

### 服务端 (Java/Quarkus)
- ✅ `BatchUpgradeRequest` DTO 已定义
- ✅ `BatchOperationResource.batchUpgrade()` API 已创建
- ✅ `BatchOperationService.createBatchUpgrade()` 已实现
- ❌ `BatchOperationService.upgradeAgentInstance()` 方法为 TODO 占位符

### Agent 端 (Go)
- ❌ 无 `UPGRADE_AGENT` 消息处理
- ❌ 无版本检测与下载逻辑
- ❌ 无升级/回滚机制

## 实现步骤

### Step 1: 服务端 - 实现升级指令发送 (2 小时)

**文件**: `server/src/main/java/com/easystation/agent/service/BatchOperationService.java`

**任务**:
1. 实现 `upgradeAgentInstance()` 方法
2. 通过 WebSocket 发送 `UPGRADE_AGENT` 消息
3. 包含下载 URL、版本号、校验和等信息

**消息格式**:
```json
{
  "type": "UPGRADE_AGENT",
  "requestId": "upgrade-xxx",
  "content": {
    "version": "0.0.2",
    "downloadUrl": "https://github.com/.../host-agent-0.0.2.tar.gz",
    "checksum": "sha256:xxx",
    "rollbackVersion": "0.0.1"
  }
}
```

### Step 2: Agent 端 - 添加升级处理模块 (4 小时)

**文件**: `agent/internal/upgrade/upgrader.go` (新建)

**任务**:
1. 创建 `Upgrader` 结构体
2. 实现版本检测逻辑
3. 实现下载与校验逻辑
4. 实现备份当前版本逻辑
5. 实现升级执行逻辑
6. 实现回滚逻辑

**目录结构**:
```
agent/internal/upgrade/
├── upgrader.go      # 升级核心逻辑
├── downloader.go    # 下载器
├── checker.go       # 版本校验
└── rollback.go      # 回滚机制
```

### Step 3: Agent 端 - 添加 WebSocket 消息处理 (1 小时)

**文件**: `agent/internal/app/agent.go`

**任务**:
1. 在 `handleMessage()` 中添加 `UPGRADE_AGENT` case
2. 调用 `Upgrader.Upgrade()` 方法
3. 异步执行升级，不阻塞主流程

### Step 4: Agent 端 - 实现升级结果上报 (1 小时)

**文件**: `agent/internal/app/agent.go`

**任务**:
1. 创建 `sendUpgradeResult()` 方法
2. 上报升级成功/失败状态
3. 包含升级日志和错误信息

**上报格式**:
```json
{
  "type": "UPGRADE_RESULT",
  "requestId": "upgrade-xxx",
  "content": {
    "success": true,
    "oldVersion": "0.0.1",
    "newVersion": "0.0.2",
    "message": "Upgrade completed successfully"
  }
}
```

### Step 5: 服务端 - 实现升级结果处理 (1 小时)

**文件**: `server/src/main/java/com/easystation/agent/websocket/AgentClientEndpoint.java`

**任务**:
1. 添加 `UPGRADE_RESULT` 消息处理
2. 更新 `BatchOperationItem` 状态
3. 记录升级日志

### Step 6: 测试与验证 (2 小时)

**测试场景**:
1. 正常升级流程测试
2. 下载失败回滚测试
3. 校验失败回滚测试
4. 升级中断回滚测试
5. 并发升级测试

## 验收标准

- [ ] 服务端可下发升级指令
- [ ] Agent 可接收并处理升级消息
- [ ] Agent 可下载并校验新版本
- [ ] 升级失败可自动回滚
- [ ] 升级结果上报服务端
- [ ] 批量升级状态可追踪

## 预计工作量

- 服务端：4 小时
- Agent 端：6 小时
- 测试：2 小时
- **总计**: 12 小时 (约 1.5 天)

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 下载超时 | 升级失败 | 支持断点续传，多次重试 |
| 校验失败 | 安全风险 | 严格 SHA256 校验，失败即回滚 |
| 升级中断 | 服务不可用 | 原子替换，保留备份版本 |
| 版本不兼容 | 运行异常 | 升级前版本兼容性检查 |

## 依赖

- GitHub Releases 或制品仓库（存放 Agent 包）
- HTTPS 下载支持
- WebSocket 连接稳定
