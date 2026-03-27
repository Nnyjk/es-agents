# 贡献指南

本文档是 Easy-Station 项目的完整贡献指南，涵盖开发环境搭建、代码提交规范、PR 流程、Issue 管理和文档规范。

> **快速参考**: GitHub 交互时可参考 [.github/CONTRIBUTING.md](../../.github/CONTRIBUTING.md)

## 目录

- [1. 开发环境搭建](#1-开发环境搭建)
- [2. 代码提交规范](#2-代码提交规范)
- [3. PR 提交与审查流程](#3-pr-提交与审查流程)
- [4. Issue 创建与管理规范](#4-issue-创建与管理规范)
- [5. 文档编写规范](#5-文档编写规范)

---

## 1. 开发环境搭建

### 系统要求

| 组件    | 版本要求 | 说明                      |
| ------- | -------- | ------------------------- |
| Node.js | 20.x     | 前端开发                  |
| Java    | 21.x     | 服务端开发 (Temurin 推荐) |
| Go      | 1.23+    | Agent 开发                |
| Maven   | 3.8+     | 服务端构建                |
| Git     | 2.x+     | 版本控制                  |
| Docker  | 24.x+    | 容器化部署（可选）        |

### 环境安装

详细的安装步骤请参考 [本地开发环境配置指南](../07-development/LOCAL-DEV-ENV.md)。

### 项目结构

```
es-agents/
├── frontend/          # React 前端应用
├── server/            # Quarkus 服务端应用
├── agent/             # Go Agent 应用
├── docs/              # 项目文档
├── scripts/           # 开发脚本
└── .github/           # GitHub 配置
```

### 快速启动

```bash
# 1. 克隆仓库
git clone https://github.com/Nnyjk/es-agents.git
cd es-agents

# 2. 前端开发
./scripts/dev-frontend.sh

# 3. 服务端开发
./scripts/dev-server.sh

# 4. Agent 开发
./scripts/dev-host-agent.sh
```

### 本地验证

```bash
# Frontend
cd frontend && npm ci --legacy-peer-deps
cd frontend && npm test
cd frontend && npm run build
cd frontend && npm run lint

# Server
cd server && mvn clean verify -DskipITs

# Agent
cd agent && go test ./...
```

---

## 2. 代码提交规范

### Conventional Commits

本项目采用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Type 类型

| Type       | 说明                   | 示例                                         |
| ---------- | ---------------------- | -------------------------------------------- |
| `feat`     | 新功能                 | `feat(frontend): add dark mode toggle`       |
| `fix`      | Bug 修复               | `fix(server): resolve auth timeout issue`    |
| `docs`     | 文档更新               | `docs: update API documentation`             |
| `style`    | 代码格式（不影响逻辑） | `style: format code with prettier`           |
| `refactor` | 重构                   | `refactor(agent): simplify connection logic` |
| `test`     | 测试                   | `test(server): add unit tests for auth`      |
| `chore`    | 构建/工具/依赖         | `chore: update dependencies`                 |
| `ci`       | CI 配置                | `ci: add GitHub Actions workflow`            |
| `perf`     | 性能优化               | `perf(server): optimize query performance`   |

### Scope 范围

- `frontend` - 前端模块
- `server` - 服务端模块
- `agent` - Agent 模块
- `docs` - 文档
- `ci` - CI/CD

### 示例

```bash
# 功能开发
feat(frontend): add user profile page

# Bug 修复
fix(server): resolve NPE in deployment handler

# 文档更新
docs(api): update server API documentation

# 重构
refactor(agent): extract common connection logic

# 破坏性变更
feat(server)!: change API response format

BREAKING CHANGE: API response now uses camelCase
```

### 提交前检查

```bash
# 推荐：安装 pre-commit hooks
pip install pre-commit
pre-commit install
pre-commit run --all-files

# 手动检查
# Frontend
cd frontend && npm run lint

# Server
cd server && mvn checkstyle:check

# Agent
cd agent && go vet ./...
```

---

## 3. PR 提交与审查流程

### 分支命名

| 类型 | 命名格式          | 示例                  |
| ---- | ----------------- | --------------------- |
| 功能 | `feat/<name>`     | `feat/user-profile`   |
| 修复 | `fix/<name>`      | `fix/auth-timeout`    |
| 重构 | `refactor/<name>` | `refactor/connection` |
| 文档 | `docs/<name>`     | `docs/api-update`     |
| 其他 | `chore/<name>`    | `chore/update-deps`   |

### 提交流程

1. **创建分支**

   ```bash
   git checkout -b feat/your-feature
   ```

2. **开发和提交**

   ```bash
   # 编写代码
   git add .
   git commit -m "feat(module): your changes"
   ```

3. **推送并创建 PR**

   ```bash
   git push origin feat/your-feature
   gh pr create
   ```

4. **填写 PR 模板**
   - 摘要：2-4 句说明变更目的
   - 关联 Issue：`Closes #xxx`
   - 变更类型：Feature/Bugfix/Refactor/Docs/CI
   - 影响范围：Frontend/Server/Agent/Docs
   - 验证方式：执行的命令和结果
   - 风险评估：影响范围和回滚方式

### 审查清单

Reviewer 会检查：

- [ ] 变更目标清晰，与 Issue 一致
- [ ] 覆盖关键路径测试
- [ ] 文档已同步更新
- [ ] 无安全/性能/兼容性风险
- [ ] 风险和回滚方式已说明

### 合并条件

- [ ] CI 检查全部通过
- [ ] 至少一位代码所有者批准
- [ ] 未解决评论已处理
- [ ] PR 模板填写完整
- [ ] 相关文档已更新

### 回滚流程

1. 确认问题范围
2. 选择回滚方式：
   - 小问题：前滚修复
   - 大问题：`git revert` 后重新 PR
3. 重新执行验证
4. 记录根因和预防措施

---

## 4. Issue 创建与管理规范

### Issue 类型

使用 GitHub Issue 模板创建：

- **Bug Report** - 报告缺陷
- **Feature Request** - 功能请求
- **Task** - 开发任务

### Issue 标签

#### 优先级标签

| 标签                | 颜色 | 说明                   |
| ------------------- | ---- | ---------------------- |
| `priority/critical` | 🔴   | 最高优先级，需立即处理 |
| `priority/high`     | 🟠   | 高优先级               |
| `priority/medium`   | 🟡   | 中优先级（默认）       |
| `priority/low`      | 🟢   | 低优先级               |

#### 模块标签

| 标签              | 说明       |
| ----------------- | ---------- |
| `module/frontend` | 前端模块   |
| `module/server`   | 服务端模块 |
| `module/agent`    | Agent 模块 |
| `module/docs`     | 文档模块   |

#### 状态标签

| 标签                 | 说明     |
| -------------------- | -------- |
| `status/todo`        | 待处理   |
| `status/in-progress` | 进行中   |
| `status/review`      | 等待审查 |
| `status/blocked`     | 被阻塞   |

#### 类型标签

| 标签            | 说明   |
| --------------- | ------ |
| `type:bug`      | 缺陷   |
| `type:feature`  | 新功能 |
| `type:docs`     | 文档   |
| `type:refactor` | 重构   |
| `type:test`     | 测试   |

### Issue 模板

#### 功能请求模板

```markdown
### 需求描述

<!-- 清晰描述功能需求 -->

### 验收标准

<!-- 如何验证功能已完成 -->

### 技术方案

<!-- 可选：实现思路 -->

### 优先级

<!-- critical/high/medium/low -->
```

#### Bug 报告模板

```markdown
### 问题描述

<!-- 简要描述问题 -->

### 复现步骤

1.
2.
3.

### 期望行为

<!-- 应该发生什么 -->

### 实际行为

<!-- 实际发生了什么 -->

### 环境信息

- 版本：
- 操作系统：
- 浏览器：

### 日志/截图

<!-- 相关日志或截图 -->
```

### Issue 状态流转

```
status:todo → status:in-progress → status:review → Closed
                    ↓
              status:blocked
```

---

## 5. 文档编写规范

### 文档结构

```
docs/
├── 00-overview/       # 项目概览
├── 01-requirements/   # 需求文档
├── 02-governance/     # 治理文档
├── 03-api/            # API 文档
├── 04-memory/         # 项目记忆
├── 05-skills/         # Agent 技能
├── 06-validation/     # 验收标准
└── 07-development/    # 开发指南
```

### 文档命名

| 类型 | 命名格式        | 示例                   |
| ---- | --------------- | ---------------------- |
| 指南 | `UPPER-CASE.md` | `CONTRIBUTING.md`      |
| 规范 | `xxx-rules.md`  | `engineering-rules.md` |
| API  | `xxx-api.md`    | `server-api.md`        |

### 文档维护规则

1. **需求基线**：以 `docs/01-requirements/easy-station-requirements.md` 为主
2. **变更说明**：文档变更必须说明背景、范围、影响面、是否兼容
3. **同步更新**：代码与文档尽量同 PR 提交

### Markdown 规范

````markdown
# 一级标题（文档标题，仅一个）

## 二级标题（章节）

### 三级标题（子章节）

**强调文本**

`代码或命令`

```语言
代码块
```
````

- 无序列表项
- 另一项

1. 有序列表项
2. 另一项

| 列1 | 列2 |
| --- | --- |
| 值1 | 值2 |

[链接文本](url)

````

### 新成员指引

新成员加入项目时，建议按以下顺序阅读：

1. `docs/README.md` - 文档索引
2. `docs/00-overview/` - 项目概览
3. `docs/01-requirements/` - 需求文档
4. `docs/07-development/LOCAL-DEV-ENV.md` - 开发环境
5. 本文档 - 贡献指南

---

## 附录

### 相关链接

- [本地开发环境配置](../07-development/LOCAL-DEV-ENV.md)
- [PR Flow 流程](../../docs/PR-FLOW.md)
- [研发规则](./engineering-rules.md)
- [API 文档](../03-api/)

### 常见问题

**Q: 如何选择正确的标签？**

A: 每个 Issue 至少需要：
- 1 个优先级标签（`priority/*`）
- 1 个模块标签（`module/*`）
- 1 个状态标签（`status/*`）

**Q: PR 提交后 CI 失败怎么办？**

A:
1. 查看 CI 日志定位问题
2. 本地复现并修复
3. 推送新的 commit
4. 等待 CI 重新运行

**Q: 如何处理冲突？**

A:
```bash
git fetch origin main
git rebase origin/main
# 解决冲突
git rebase --continue
git push -f origin your-branch
````

---

_最后更新: 2026-03-27_
