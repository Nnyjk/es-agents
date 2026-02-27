# Server-HostAgent 联通性验证指南

本文档用于指导在正式部署前验证 Server 与 HostAgent 之间的联通性。

## 前置条件

1. **Server 端已启动**
   - 监听端口：8080（默认）
   - WebSocket 路径：`/ws/console/{hostId}`

2. **HostAgent 已部署**
   - 监听端口：9090（默认）
   - WebSocket 路径：`/ws`

3. **网络可达**
   - Server 能够访问 Host 的 `gatewayUrl`
   - 防火墙已开放相应端口

## 验证步骤

### 步骤 1：检查 Host 配置

在 Server 数据库中确认 Host 记录：

```sql
SELECT id, name, hostname, gatewayUrl, listenPort, status, "secretKey" 
FROM infra_host 
WHERE name = 'your-host-name';
```

**关键字段检查：**
- `gatewayUrl`: Server 连接 HostAgent 的地址（如 `http://192.168.1.100:9090`）
- `listenPort`: HostAgent 监听端口（默认 9090）
- `secretKey`: 用于 HMAC 认证的密钥
- `status`: 应为 `UNCONNECTED`（初始状态）

### 步骤 2：验证 HostAgent 运行状态

在目标主机上检查 HostAgent 进程：

```bash
# Linux/macOS
ps aux | grep host-agent
netstat -tlnp | grep 9090

# Windows
tasklist | findstr host-agent
netstat -ano | findstr 9090
```

**预期输出：**
- HostAgent 进程正在运行
- 端口 9090 处于 LISTEN 状态

### 步骤 3：测试 WebSocket 连接

#### 方法 A：使用 wscat 工具

```bash
# 安装 wscat
npm install -g wscat

# 测试连接（替换为实际的 gatewayUrl 和 hostId）
wscat -c ws://192.168.1.100:9090/ws -H "X-Agent-Secret: your-secret-key"
```

**预期结果：**
- 连接成功建立
- 可以发送和接收消息

#### 方法 B：使用 Server 的连接测试功能

在 Server 端调用连接接口：

```bash
curl -X POST http://localhost:8080/infra/hosts/{hostId}/connect \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**预期响应：**
- HTTP 200 OK
- Host 状态更新为 `ONLINE`

### 步骤 4：验证心跳机制

HostAgent 应定期发送心跳消息到 Server。检查 Server 日志：

```bash
# 查看 Server 日志
tail -f server/logs/server.log | grep -i heartbeat
```

**预期日志：**
```
[INFO] Heartbeat received from host {hostId}
[INFO] Updated host {hostId} lastHeartbeat timestamp
```

### 步骤 5：测试命令执行

通过前端界面或 API 执行测试命令：

1. 在前端打开主机终端
2. 执行简单命令：`echo "Hello from HostAgent"`
3. 检查是否能收到执行结果

**预期结果：**
- 命令成功执行
- 返回输出内容

## 常见问题排查

### 问题 1：连接超时

**症状：** Server 无法连接到 HostAgent

**可能原因：**
- 防火墙阻止连接
- HostAgent 未启动
- `gatewayUrl` 配置错误

**解决方案：**
```bash
# 检查防火墙
sudo ufw status  # Ubuntu
sudo firewall-cmd --list-all  # CentOS

# 开放端口
sudo ufw allow 9090/tcp
```

### 问题 2：认证失败

**症状：** WebSocket 连接被拒绝，错误 401

**可能原因：**
- `secretKey` 不匹配
- 请求头格式错误

**解决方案：**
1. 确认数据库中的 `secretKey` 与 HostAgent 配置一致
2. 检查请求头名称：`X-Agent-Secret`（区分大小写）

### 问题 3：心跳丢失

**症状：** Host 状态频繁在 ONLINE/OFFLINE 之间切换

**可能原因：**
- 网络不稳定
- 心跳间隔设置过短
- HostAgent 负载过高

**解决方案：**
1. 调整心跳间隔（默认 30 秒）：
   ```sql
   UPDATE infra_host SET "heartbeatInterval" = 60 WHERE id = '{hostId}';
   ```
2. 检查网络质量
3. 检查 HostAgent 资源使用情况

### 问题 4：WebSocket 连接立即断开

**症状：** 连接建立后立即关闭

**可能原因：**
- 协议版本不匹配
- 消息格式错误

**解决方案：**
1. 确认 Server 和 HostAgent 使用相同的协议版本（v2.0）
2. 检查消息信封格式是否符合规范

## 检查清单

在正式上线前，请确认以下项目：

- [ ] Host 配置正确（gatewayUrl、secretKey、listenPort）
- [ ] HostAgent 进程正常运行
- [ ] 网络端口开放且可达
- [ ] WebSocket 连接成功建立
- [ ] 心跳消息正常发送/接收
- [ ] 命令执行功能正常
- [ ] 日志记录完整
- [ ] 错误处理机制有效

## 安全建议

1. **生产环境必须使用 WSS**
   - 配置 SSL 证书
   - 更新 `gatewayUrl` 为 `wss://` 协议

2. **定期轮换密钥**
   - 建议每 90 天更新一次 `secretKey`
   - 更新后重启 HostAgent

3. **限制访问来源**
   - 配置防火墙只允许 Server IP 访问 9090 端口
   - 使用白名单机制

## 相关文档

- [通信协议规范](./01-communication-logic.md)
- [HostAgent 部署指南](../05-skills/host-agent-deployment.md)
- [API 参考](./server-api.md)
