# Issue #51: Agent 部署流程 WebSocket 实时状态推送

## 目标
实现部署状态变更时通过 WebSocket 实时推送给前端。

## 实现内容

### 1. 部署 WebSocket 端点
**文件**: `server/src/main/java/com/easystation/deployment/websocket/DeploymentWebSocket.java`

创建 WebSocket 端点：
- 路径：`/ws/deployment/{deploymentId}`
- 管理会话：ConcurrentHashMap<deploymentId, Set<Session>>
- 发送消息类型：DEPLOYMENT_PROGRESS, DEPLOYMENT_STATUS, DEPLOYMENT_ERROR

### 2. 部署进度推送服务
**文件**: `server/src/main/java/com/easystation/deployment/service/DeploymentPushService.java`

提供方法：
- `pushProgress(String deploymentId, int progress, String message)`
- `pushStatus(String deploymentId, String status)`
- `pushError(String deploymentId, String error)`
- `broadcastToDeployment(String deploymentId, String message)`

### 3. 集成到部署流程
修改以下服务在状态变更时调用推送：
- `DeploymentService.executeDeployment()` - 开始/完成时推送
- `DeploymentHistoryService.updateStatus()` - 状态更新时推送

### 4. 消息格式
```json
{
  "type": "DEPLOYMENT_PROGRESS",
  "deploymentId": "uuid",
  "status": "RUNNING|SUCCESS|FAILED|ROLLBACK",
  "progress": 0-100,
  "message": "当前步骤描述",
  "timestamp": "2026-03-28T12:00:00Z"
}
```

## 验收标准
- [x] 部署状态变更自动推送
- [x] 支持进度更新推送
- [x] 支持错误信息推送
- [x] Java 代码通过编译验证

## 关联
- Closes #51
