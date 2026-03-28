# 任务：Agent 状态机完整实现 (#38)

## 背景
需求文档定义 Agent 状态机，当前已实现部分状态字段，但未实现完整的状态流转逻辑。

## 当前状态
- AgentStatus 枚举已定义 10 种状态：UNCONFIGURED, PREPARING, READY, PACKAGING, PACKAGED, DEPLOYING, DEPLOYED, ONLINE, OFFLINE, ERROR
- AgentStatusSnapshot 实体已创建用于记录状态历史
- 缺少状态流转规则和校验逻辑

## 需求
1. 完整的状态枚举定义（已完成）
2. 状态流转规则与校验
3. 状态变更事件记录
4. 定时任务处理超时/失败状态

## 需要创建的文件
1. `server/src/main/java/com/easystation/agent/service/AgentStateMachineService.java` - 状态机服务
2. `server/src/main/java/com/easystation/agent/domain/AgentStateTransition.java` - 状态流转规则定义
3. `server/src/main/java/com/easystation/agent/resource/AgentStateResource.java` - 状态流转 API

## 状态流转规则
```
UNCONFIGURED → PREPARING → READY
READY → PACKAGING → PACKAGED → DEPLOYING → DEPLOYED → ONLINE
ONLINE ↔ OFFLINE
任何状态 → ERROR
ERROR → READY (恢复)
```

## API 端点
- `POST /v1/agents/{id}/transition` - 状态流转（带校验）
- `GET /v1/agents/{id}/transitions` - 获取可用流转
- `GET /v1/agents/{id}/state-history` - 状态变更历史

## 验收标准
- [x] 完整的状态枚举定义
- [ ] 状态流转 API 实现
- [ ] 状态变更历史记录查询
- [ ] 超时/失败状态处理（定时任务）

## 执行方式
使用 claude-code-executor skill 执行此任务。
