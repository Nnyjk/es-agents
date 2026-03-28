## 描述
实现部署状态变更时通过 WebSocket 实时推送给前端。

## 更改
- 添加 DeploymentWebSocket 端点 (/ws/deployment/{deploymentId})
- 添加 DeploymentProgressService 推送方法
- 集成到部署流程，状态变更时自动推送
- 支持进度、状态、错误信息推送

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
- Closes #51
