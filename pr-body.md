## 实现内容

为 Agent 实例添加完整的部署流程支持：

### 新增功能
- **DTO**: 添加 `Deploy` 和 `DeployResult` 记录类型
- **Service**: 实现 `AgentInstanceService.deploy()` 方法
  - 前置状态校验（仅允许 READY 或 PACKAGED 状态）
  - 状态流转：DEPLOYING -> DEPLOYED/ERROR
- **API**: 添加 `POST /api/v1/agents/instances/{id}/deploy` 端点

### 变更文件
- `server/src/main/java/com/easystation/agent/dto/AgentInstanceRecord.java`
- `server/src/main/java/com/easystation/agent/service/AgentInstanceService.java`
- `server/src/main/java/com/easystation/agent/resource/AgentInstanceResource.java`

### 待完善
- 实际的部署逻辑（WebSocket/SSH 传输安装包）
- 部署结果验证
- 回滚机制

## 关联 Issue

Closes #11