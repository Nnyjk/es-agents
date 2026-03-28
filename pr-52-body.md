## 描述
完成部署流程前端的实时进度展示功能，集成 WebSocket 推送。

## 功能
- **WebSocket 实时推送**: 连接 `/ws/deployment/{instanceId}` 接收部署进度
- **6 个部署阶段**: PREPARING → PACKAGING → UPLOADING → DEPLOYING → HEALTH_CHECK → COMPLETED
- **自动重连机制**: 5 次尝试，3 秒间隔
- **连接状态指示器**: 显示 WebSocket 连接状态
- **错误处理**: 错误提示与失败状态展示

## 更改
- `frontend/src/types/index.ts`: 添加 DeploymentStatus, DeploymentMessageType 等类型
- `frontend/src/pages/goals/DeploymentWizard/components/DeployExecution.tsx`: 集成 WebSocket，实现实时进度展示

## 消息格式
```json
{
  "type": "DEPLOYMENT_PROGRESS|DEPLOYMENT_STATUS|DEPLOYMENT_ERROR",
  "deploymentId": "uuid",
  "status": "PENDING|RUNNING|SUCCESS|FAILED",
  "progress": 0-100,
  "stage": "当前阶段",
  "message": "描述信息",
  "timestamp": "ISO8601"
}
```

## 关联
- Closes #52
- 依赖 #51 (部署流程 WebSocket 实时推送) ✅
- 依赖 #42 (Agent 任务结果回调 API) ✅
