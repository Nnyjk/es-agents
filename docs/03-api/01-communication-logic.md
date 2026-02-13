# 01 交互逻辑与通信契约 (Communication Logic)

## 1. 概述
本文档定义 Easy-Station 系统中各组件间的通信逻辑与契约。主要包含两部分：
1. **Server-Frontend**：基于 HTTP RESTful 的 API 交互。
2. **Server-Agent**：基于 Gateway/Proxy 的 **Server 主动连接** 模式（Server-Initiated Connection）。

## 2. Server-Frontend 交互逻辑 (RESTful API)

### 2.1 设计原则
* **资源导向**：API URL 设计应清晰表达资源层级（如 `/environments/{id}/hosts`），名词复数，连字符命名。
* **HTTP 动词语义**：严格遵守 HTTP Method 语义（GET 查询, POST 创建, PUT/PATCH 修改, DELETE 删除）。
* **无状态**：Server 端不保存会话状态，认证依赖 JWT Token。

### 2.2 响应与错误处理
* **成功响应**：
  * 直接返回资源对象或资源列表。
  * 分页数据采用包裹结构，包含 `items`, `total`, `page`, `size` 等元数据。
* **错误响应**：
  * HTTP 状态码必须准确反映错误类别（400 参数错, 401 未认证, 403 无权限, 404 未找到, 500 系统误）。
  * 响应体包含标准错误结构：`code` (错误码), `message` (可读信息), `details` (详细上下文)。

### 2.3 数据格式
* 所有交互统一使用 JSON 格式。
* 时间字段统一使用 ISO 8601 标准字符串。
* ID 字段统一为字符串类型（UUID 或 Stringified Long）。

## 3. Server-Agent 通信协议 (Agent Protocol)

### 3.1 通信模型与网络约束（唯一事实源）
Server 与 Host Agent 采用 **Server 主动连接 Agent** 的模式。

**链路方向**：
* **Server -> Host Agent**：Server 根据 Host 的 `gatewayUrl` 主动建立 WebSocket 连接到 Agent `/ws`。
* **Host Agent -> Server**：不额外发起独立控制连接，基于已建立连接进行上报与回传。

### 3.2 交互流程
1. **连接建立**
   * Host Agent 启动并监听本地端口（默认 9090）。
   * Server 根据 `gatewayUrl` 转换为 `ws/wss` 后连接 `/ws`。
   * 握手时 Server 必须携带 `X-Agent-Secret`。
2. **指令下发**
   * Frontend -> Server（Console WS）。
   * Server -> Agent（复用已建立连接）。
3. **执行回传**
   * Agent -> Server 返回 HEARTBEAT / LOG / 执行结果。
   * Server 持久化日志并广播到 Console WS。

### 3.3 消息信封（Protocol v2）
统一采用：
```json
{
  "protocolVersion": "2.0",
  "requestId": "uuid",
  "timestamp": "2026-02-13T12:34:56Z",
  "type": "EXEC_CMD",
  "content": {},
  "error": null
}
```

字段约束：
* `protocolVersion`：协议版本；默认 `2.0`。
* `requestId`：请求幂等键与链路追踪键。
* `timestamp`：发送时间（UTC ISO8601）。
* `type`：消息类型。
* `content`：业务载荷。
* `error`：失败时填充 `{code, message, details}`。

### 3.4 消息类型
1. **HEARTBEAT**（Agent -> Server）
2. **LOG**（Agent -> Server -> Frontend）
3. **FETCH_LOGS**（Frontend -> Server）
4. **LOG_HISTORY**（Server -> Frontend）
5. **EXEC_CMD**（Frontend -> Server -> Agent）
6. **EXEC_RESULT**（Agent -> Server -> Frontend）
7. **INPUT**（Frontend -> Server -> Agent）

### 3.5 可观测性要求
* 所有下发消息必须携带 `requestId`，并在 Server 日志记录 `hostId + requestId + type`。
* 命令结果最少应包含：`status, exitCode, startedAt, finishedAt, durationMs`。
* 日志保留原始文本，同时支持按 `requestId` 聚合查询。

### 3.6 安全治理要求
* 握手鉴权：`X-Agent-Secret` 必填。
* 最小暴露：Agent 仅暴露 `/ws`。
* 禁止明文公网传输：生产环境必须使用 `wss`。
* 密钥轮换：支持 Host 级密钥更新并可平滑重连。

### 3.7 异常处理逻辑
* **连接失败**：Server 将 Host 标记为 `OFFLINE/EXCEPTION` 并按策略重试。
* **指令超时**：Agent 侧终止超时任务并回传 `EXEC_RESULT(status=TIMEOUT)`。
* **链路中断**：Server 对未完成任务标记 `UNKNOWN`，并提示用户重试或恢复。
