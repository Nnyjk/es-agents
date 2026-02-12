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

详细文档请参考 `docs/` 目录。
