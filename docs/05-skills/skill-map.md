# Skill 体系说明（Skill Map）

> 本文档面向“AI 协作 / 自动化执行”场景，说明仓库级 skill 使用边界。

## 1. 当前可用 skill（会话环境提供）
- `skill-creator`：用于创建或更新技能包（SKILL.md 与相关资产）。
- `skill-installer`：用于安装可复用技能到本地 Codex skill 目录。

## 2. 使用原则
- 只有在任务明确要求“创建/更新 skill”时，才启用 `skill-creator`。
- 只有在任务明确要求“安装 skill”时，才启用 `skill-installer`。
- 普通业务开发、文档整理、Bug 修复，不应强行引入 skill 流程。

## 3. 推荐触发语义
- 触发 `skill-creator`：
  - “帮我新增一个 skill”
  - “把这个流程沉淀成 SKILL.md”
- 触发 `skill-installer`：
  - “安装某个 skill”
  - “列出可安装 skill 并安装”

## 4. 与项目文档的关系
- Skill 文档不替代业务需求文档。
- Skill 产出应引用 `docs/01-requirements` 与 `docs/03-api`，保证与业务契约一致。
