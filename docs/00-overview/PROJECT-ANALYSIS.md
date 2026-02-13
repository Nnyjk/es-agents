# 项目现状分析（2026-02）

## 1. 仓库结构
- `frontend/`：基于 Vite + React + TypeScript 的前端管理台。
- `server/`：基于 Java（Quarkus 风格）服务端，负责 RBAC、基础设施管理、Agent 管理与 WebSocket。
- `agent/`：基于 Go 的 Host Agent（Runner）与通信组件。
- `docs/`：需求与通信文档（存在主题交叉与索引缺失问题）。

## 2. 架构分层（从代码与文档交叉验证）
- 控制面：`server` 提供 REST API、WebSocket、调度与持久化。
- 数据面：`agent` 负责命令执行、日志回传、心跳、插件调度。
- 展现层：`frontend` 提供环境/主机/Agent/模板等页面。
- 外部依赖：Proxy/Gateway 作为消息转发链路（非本仓库实现）。

## 3. 当前文档风险（2026-02）
1. 需求文档重复：`01-product-definition.md`、`02-functional-specs.md`、`easy-station-requirements.md` 仍有语义重叠。
2. 协议演进风险：通信协议信封虽已定义，但 server/agent 仍存在渐进迁移中的兼容路径。
3. 文档与实现一致性风险：规则更新后，需要持续做联动检查，避免再次漂移。

## 4. 当前优化重点
- 清理失效、过期和重复内容（文档与代码）。
- 继续收敛协议实现，减少 legacy 路径。
- 用最小必要文档维持可维护性。

## 5. 后续建议
- 新需求优先更新 `docs/01-requirements/easy-station-requirements.md`，其他历史文档仅做参考。
- 每次里程碑后在 `docs/04-memory/project-memory.md` 更新稳定结论。
- 在 CI 增加 docs 链接与关键术语一致性检查。
