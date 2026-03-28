# Issue #51: Agent 部署流程 WebSocket 实时状态推送

## 目标
实现部署状态变更时通过 WebSocket 实时推送给前端，让前端实时感知部署进度。

## 实现内容

### 1. 部署 WebSocket 端点 (`server/src/main/java/com/easystation/deployment/websocket/DeploymentWebSocket.java`)
创建新的 WebSocket 端点：
- 路径：`/ws/deployment/{deploymentId}`
- 支持前端订阅特定部署任务的进度
- 发送部署状态变更、进度百分比、错误信息

### 2. 部署进度推送服务 (`server/src/main/java/com/easystation/deployment/service/DeploymentProgressService.java`)
- 管理部署 WebSocket 会话
- 提供推送方法：`pushProgress(deploymentId, progress)`
- 提供推送方法：`pushStatus(deploymentId, status)`
- 提供推送方法：`pushError(deploymentId, error)`

### 3. 集成到部署流程
修改部署相关服务，在状态变更时调用推送服务：
- `DeploymentService.executeDeployment()` - 开始部署时推送
- `DeploymentHistoryService.updateStatus()` - 状态更新时推送
- 错误处理时推送错误信息

### 4. 消息格式
```json
{
  "type": "DEPLOYMENT_PROGRESS",
  "deploymentId": "uuid",
  "status": "RUNNING|SUCCESS|FAILED",
  "progress": 0-100,
  "message": "当前步骤描述",
  "timestamp": "2026-03-28T12:00:00Z"
}
```

## 验收标准
- [ ] 部署状态变更自动推送
- [ ] 支持进度更新推送
- [ ] 支持错误信息推送
- [ ] Java 代码通过编译验证
- [ ] 不影响现有部署流程

## 关联
- Closes #51
- 依赖：#42 (已完成)
