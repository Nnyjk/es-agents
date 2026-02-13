# Agent-Server WebSocket 通信协议

## 1. 概述
本文档定义 Server 与 Host Agent 之间的通信协议。
通信采用 WebSocket 长连接，实现双向实时交互。

- **连接方向**: Server 主动连接 Host Agent
- **Endpoint**: `ws://{host}:{port}/ws`
- **Protocol**: `ws` (开发) / `wss` (生产)

## 2. 连接与鉴权
Server 在建立 WebSocket 连接时，必须在 HTTP 握手阶段提供鉴权凭证。

### 2.1 握手头 (Handshake Headers)
| Header | 说明 | 必填 |
| :--- | :--- | :--- |
| `X-Agent-Secret` | 主机通信密钥 (Host Secret Key) | 是 |

若密钥校验失败，Agent 应返回 `401 Unauthorized`。

## 3. 消息信封（建议统一）
```json
{
  "protocolVersion": "2.0",
  "requestId": "uuid",
  "timestamp": "2026-02-13T12:34:56Z",
  "type": "HEARTBEAT",
  "content": {},
  "error": null
}
```

## 4. 上行消息 (Agent -> Server)

### 4.1 心跳 (HEARTBEAT)
```json
{
  "type": "HEARTBEAT",
  "content": {
    "agentId": "uuid-string",
    "status": "ONLINE",
    "timestamp": "2026-02-13T12:34:56",
    "version": "0.0.1",
    "osType": "linux"
  }
}
```

### 4.2 实时日志 (LOG)
```json
{
  "type": "LOG",
  "content": "[INFO] Agent started successfully..."
}
```

### 4.3 执行结果 (EXEC_RESULT)
```json
{
  "type": "EXEC_RESULT",
  "content": {
    "status": "SUCCESS",
    "exitCode": 0,
    "startedAt": "2026-02-13T12:34:56Z",
    "finishedAt": "2026-02-13T12:35:03Z",
    "durationMs": 7000
  }
}
```

## 5. 下行消息 (Server -> Agent)

### 5.1 命令执行 (EXEC_CMD)
```json
{
  "type": "EXEC_CMD",
  "requestId": "uuid",
  "content": {
    "command": "ls -la",
    "timeoutMs": 30000
  }
}
```

### 5.2 日志历史请求 (FETCH_LOGS)
```json
{
  "type": "FETCH_LOGS"
}
```

## 6. 错误处理
- 连接断开：Server 按重试策略重连，Host 标记状态变更。
- 鉴权失败：Server 记录安全日志并将 Host 标记为异常。
- 指令超时：Agent 回传 `EXEC_RESULT(status=TIMEOUT)`。
- 协议错误：返回标准 `error` 字段，包含 `code/message/details`。
