# ESA 监控配置

本目录包含 ESA 项目的 Prometheus + Grafana 监控配置。

## 目录结构

```
monitoring/
├── docker-compose.yml          # Prometheus + Grafana 部署配置
├── prometheus/
│   └── prometheus.yml          # Prometheus 抓取配置
└── grafana/
    ├── datasources/
    │   └── prometheus.yml      # Grafana 数据源配置
    └── dashboards/
        └── esa-system-overview.json  # 系统概览 Dashboard
```

## 快速开始

### 1. 启动监控栈

```bash
cd monitoring
docker-compose up -d
```

### 2. 访问服务

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/esa-admin)

### 3. 配置指标抓取

确保以下端点可访问：

- Server metrics: `http://localhost:8080/q/metrics`
- Agent metrics: `http://localhost:9090/metrics`

Prometheus 默认配置为从 `host.docker.internal` 抓取指标。

## 指标说明

### Agent 指标

| 指标名称 | 类型 | 说明 |
|---------|------|------|
| `esa_agent_status` | Gauge | Agent 状态 (1=Online, 0=Offline) |
| `esa_agent_task_executions_total` | Counter | 任务执行总数 |
| `esa_agent_task_execution_duration_seconds` | Histogram | 任务执行延迟分布 |
| `esa_host_cpu_usage_percent` | Gauge | CPU 使用率 |
| `esa_host_memory_usage_percent` | Gauge | 内存使用率 |
| `esa_host_disk_usage_percent` | Gauge | 磁盘使用率 |
| `esa_agent_websocket_connections` | Gauge | WebSocket 连接数 |
| `esa_agent_plugin_task_success_total` | Counter | 插件任务成功数 |
| `esa_agent_plugin_task_failure_total` | Counter | 插件任务失败数 |

### Server 指标

| 指标名称 | 类型 | 说明 |
|---------|------|------|
| `api_requests_total` | Counter | API 请求总数 |
| `api_request_duration_seconds` | Histogram | API 请求延迟分布 |
| `deployments_total` | Gauge | 部署总数 |
| `agents_online` | Gauge | 在线 Agent 数 |
| `active_users` | Gauge | 活跃用户数 |
| JVM 指标 | Various | JVM 内存、GC、线程等 |

## Grafana Dashboard

预置 Dashboard 包含以下面板：

1. **系统状态**
   - Agent 状态 (Online/Offline)
   - WebSocket 连接数
   - 任务执行总数
   - 任务成功率

2. **主机资源**
   - CPU 使用率
   - 内存使用率
   - 磁盘使用率

3. **任务执行**
   - 任务执行延迟 (p50/p90/p99)

4. **API 指标**
   - API 请求速率

## 自定义配置

### 修改 Prometheus 抓取目标

编辑 `prometheus/prometheus.yml`，修改 `static_configs.targets`。

### 添加新 Dashboard

将 JSON 文件放入 `grafana/dashboards/` 目录，Grafana 会自动加载。

### 修改 Grafana 密码

编辑 `docker-compose.yml` 中的 `GF_SECURITY_ADMIN_PASSWORD` 环境变量。

## 故障排查

### Prometheus 无法抓取指标

1. 检查目标端点是否可访问：
   ```bash
   curl http://localhost:8080/q/metrics
   curl http://localhost:9090/metrics
   ```

2. 检查 Docker 网络配置：
   ```bash
   docker-compose logs prometheus
   ```

### Grafana Dashboard 不显示数据

1. 确认 Prometheus 数据源配置正确
2. 检查指标名称是否与 Dashboard 中一致
3. 查看 Grafana 日志：
   ```bash
   docker-compose logs grafana
   ```

## 清理

```bash
docker-compose down -v  # 删除容器和数据卷
```
