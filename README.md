# Easy-Station

Easy-Station 是一个目标驱动（Goal-Driven）的自动化运维与部署平台。

## 核心功能
- **目标驱动交互**：围绕“部署 Agent”、“执行命令”等目标设计 UI。
- **Server-Agent 分离**：控制面与数据面解耦，通过 Proxy 通信。
- **Agent 插件化**：Host Agent (Runner) 负责调度，Agent (Plugin) 负责具体能力扩展。

## 目录结构
- `docs/`: 项目文档（需求、架构、API、手册）。
- `server/`: 服务端代码。
- `agent/`: Agent 端代码。
- `frontend/`: 前端代码。

详细文档请先阅读 `docs/00-overview/DOCS-INDEX.md`，再按索引进入各专题。

## 本地调试

常用本地调试脚本位于 `scripts/`：

- `./scripts/dev-frontend.sh`：启动前端开发服务器。
- `./scripts/dev-server.sh`：以 Quarkus dev mode 启动服务端，默认连接本机 PostgreSQL。
- `./scripts/dev-host-agent-config.sh`：生成本地 `host-agent` 调试配置。
- `./scripts/dev-host-agent.sh`：使用调试配置启动 `host-agent`。

完整说明见 `docs/07-development/LOCAL-DEV-ENV.md`。
