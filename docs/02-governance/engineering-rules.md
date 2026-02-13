# 研发规则（Engineering Rules）

## 1. 文档维护规则
- 需求基线：以 `docs/01-requirements/easy-station-requirements.md` 为主。
- 历史文档：`01-product-definition.md`、`02-functional-specs.md`、`03-user-interaction.md` 作为补充，不重复扩写同一需求。
- 文档变更必须说明：背景、范围、影响面、是否兼容。

## 2. 协议变更规则
- 涉及 Agent 通信消息类型、字段、状态机时，必须同步更新：
  - `docs/03-api/01-communication-logic.md`
  - `docs/03-api/agent-protocol.md`
- 涉及前后端 REST 契约时，必须同步更新 `docs/03-api/server-api.md`。

## 3. 需求变更规则
- 每个新增功能至少补充：用户目标、成功条件、失败处理、权限边界。
- 若影响部署/命令执行链路，必须补充审计与可观测要求。

## 4. 记忆更新规则
- 每次里程碑/重要决策完成后，更新 `docs/04-memory/project-memory.md`：
  - 决策内容
  - 决策原因
  - 影响范围
  - 未决事项

## 5. 协作规则
- 新成员先读 Docs Index，再读需求主文档。
- 代码与文档提交尽量同 PR，避免“代码领先文档”长期失配。
