# 01 交互逻辑与通信契约 (Communication Logic)

## 1. 概述
本文档定义 Easy-Station 系统中各组件间的通信逻辑与契约。主要包含两部分：
1.  **Server-Frontend**：基于 HTTP RESTful 的 API 交互。
2.  **Server-Agent**：基于 Gateway/Proxy 的 Server 主动连接模式。

## 2. Server-Frontend 交互逻辑 (RESTful API)

### 2.1 设计原则
*   **资源导向**：API URL 设计应清晰表达资源层级（如 `/environments/{id}/hosts`），名词复数，连字符命名。
*   **HTTP 动词语义**：严格遵守 HTTP Method 语义（GET 查询, POST 创建, PUT/PATCH 修改, DELETE 删除）。
*   **无状态**：Server 端不保存会话状态，认证依赖 JWT Token。

### 2.2 响应与错误处理
*   **成功响应**：
    *   直接返回资源对象或资源列表。
    *   分页数据采用包裹结构，包含 `items`, `total`, `page`, `size` 等元数据。
*   **错误响应**：
    *   HTTP 状态码必须准确反映错误类别（400 参数错, 401 未认证, 403 无权限, 404 未找到, 500 系统误）。
    *   响应体包含标准错误结构：`code` (错误码), `message` (可读信息), `details` (详细上下文)。

### 2.3 数据格式
*   所有交互统一使用 JSON 格式。
*   时间字段统一使用 ISO 8601 标准字符串。
*   ID 字段统一为字符串类型（UUID 或 Stringified Long）。

## 3. Server-Agent 通信协议 (Agent Protocol)

### 3.1 通信模型与网络约束
Server 与 Host Agent 之间采用 **Server 主动连接** 的模式（Server-Initiated Connection）。通常通过 Gateway 或直接连接。

**关键网络约束**：
*   **Server -> Host Agent**：Server 必须能够访问 Host Agent 监听的端口（直接访问或通过网关/NAT映射）。
*   **Host Agent -> Server (禁止直连)**：Host Agent **不需要也不允许** 主动连接 Server。所有通信链路建立均由 Server 发起。

### 3.2 交互流程
1.  **连接建立 (Connection Establishment)**
    *   Host Agent 启动并监听配置的本地端口 (如 9090)。
    *   Server 根据配置的 `gatewayUrl` (指向 Host Agent 的地址) 发起连接请求 (HTTP/WebSocket/gRPC)。
    *   连接建立后，双方进行握手与认证。

2.  **指令下发 (Server -> Agent)**
    *   Server 通过已建立的连接发送指令。
    *   Host Agent 接收并执行。

3.  **数据上报 (Agent -> Server)**
    *   Host Agent 执行完成后，通过同一连接（响应或回调）返回执行结果。
    *   实时日志通过连接流式传输。

### 3.3 消息类型定义 (JSON Protocol v2)
通信采用 JSON 格式：`{"type": "...", "content": ...}`。

1.  **LOG (实时日志)**
    *   **方向**: Agent -> Server -> Frontend
    *   **内容**: `{"type": "LOG", "content": "Log message..."}`
    *   **说明**: Agent 产生日志时实时推送，Server 持久化存储并广播至前端。

2.  **FETCH_LOGS (获取日志历史)**
    *   **方向**: Frontend -> Server
    *   **内容**: `{"type": "FETCH_LOGS"}`
    *   **说明**: 前端终端连接建立时发送，Server 从本地日志文件中读取最近 100 行返回。

3.  **LOG_HISTORY (日志历史响应)**
    *   **方向**: Server -> Frontend
    *   **内容**: `{"type": "LOG_HISTORY", "content": ["line1", "line2"]}`
    *   **说明**: 响应 `FETCH_LOGS` 请求。

4.  **EXEC_CMD (执行命令)**
    *   **方向**: Frontend -> Server -> Agent
    *   **内容**: `{"type": "EXEC_CMD", "content": {"command": "ls -la"}}`
    *   **说明**: 执行一次性 Shell 命令。

5.  **INPUT (终端输入)**
    *   **方向**: Frontend -> Server -> Agent
    *   **内容**: `{"type": "INPUT", "content": {"content": "char"}}`
    *   **说明**: 标准输入流（目前 Agent 暂未完全实现 PTY，主要用于简单交互）。

6.  **HEARTBEAT (心跳)**
    *   **方向**: Agent -> Server
    *   **内容**: `{"type": "HEARTBEAT", "content": "timestamp"}`
    *   **说明**: 维持连接活跃状态。

### 3.4 安全机制
*   **Secret Key 认证**：
    *   每个 Host Agent 在 config.yaml 中配置 `secret_key`。
    *   Server 发起连接时，必须携带对应的凭证。
    *   Host Agent 校验凭证，通过则接受连接，否则拒绝。

### 3.5 异常处理逻辑
*   **连接失败**：Server 若无法连接 Host Agent（如网络不通、端口未开），应标记 Host 为离线，并定期重试。
*   **指令超时**：
    *   **Agent 侧**：执行脚本若超过 payload 指定的 timeout，应强制终止进程并上报 TIMEOUT 状态。
    *   **Server 侧**：若在规定时间内未收到 COMMAND_RESULT，Server 将该命令标记为超时。
