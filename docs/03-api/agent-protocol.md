# Agent-Server WebSocket 通信协议

## 1. 概述
本文档定义 Server 与 Host Agent 之间的通信协议。
通信采用 WebSocket 长连接，实现双向实时交互。

- **Endpoint**: `/ws/agent/{agentId}`
- **Protocol**: `ws` (开发) / `wss` (生产)

## 2. 连接与鉴权
Host Agent 在建立 WebSocket 连接时，必须在 HTTP 握手阶段提供鉴权凭证。

### 2.1 握手头 (Handshake Headers)
| Header | 说明 | 必填 |
| :--- | :--- | :--- |
| `X-Agent-Secret` | 主机通信密钥 (Host Secret Key) | 是 |

若密钥校验失败，Server 将拒绝连接并返回 Close Code `CANNOT_ACCEPT` (1003)。

## 3. 上行消息 (Agent -> Server)
Agent 发送的消息均为 JSON 格式。

### 3.1 心跳 (Heartbeat)
Agent 定期发送心跳包以维持连接并上报状态。

**Payload**:
```json
{
  "agentId": "uuid-string",
  "version": "1.0.0",
  "hostname": "server-01",
  "ip": "192.168.1.10",
  "os": "Linux",
  "arch": "amd64",
  "cpuCores": 4,
  "memoryTotal": 16384,
  "diskTotal": 102400
}
```

### 3.2 实时日志 (Log Stream)
Agent 将运行日志实时推送给 Server。

**Payload**:
```json
{
  "type": "LOG",
  "content": "[INFO] Agent started successfully..."
}
```

## 4. 下行消息 (Server -> Agent)
Server 通过 WebSocket 向 Agent 下发指令。

### 4.1 命令执行
Server 发送纯文本或 JSON 格式的命令字符串，Agent 接收后解析执行。
(具体命令格式待后续 Command 模块详细定义)

## 5. 错误处理
- 连接断开：Agent 应实现指数退避重连机制。
- 鉴权失败：Agent 应停止重连并记录错误日志，等待人工介入更新配置。
