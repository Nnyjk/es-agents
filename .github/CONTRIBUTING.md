# 贡献指南

感谢你为 Easy-Station 项目做出贡献！

## 🚀 快速开始

### 1. Fork 项目

在 GitHub 上点击 Fork 按钮

### 2. 克隆仓库

```bash
git clone https://github.com/YOUR_USERNAME/es-agents.git
cd es-agents
```

### 3. 创建分支

```bash
git checkout -b feature/your-feature-name
```

## 📝 开发流程

### 提交规范

我们使用 Conventional Commits 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Type 类型**:

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

**示例**:

```
feat(frontend): add dark mode toggle

fix(server): resolve authentication timeout issue

docs: update API documentation
```

### 提交前检查

1. **代码风格**

   ```bash
   # Frontend
   cd frontend && npm run lint

   # Server
   cd server && mvn checkstyle:check
   ```

2. **测试**

   ```bash
   # Frontend
   cd frontend && npm test

   # Server
   cd server && mvn test

   # Agent
   cd agent && go test ./...
   ```

3. **构建**

   ```bash
   # Frontend
   cd frontend && npm run build

   # Server
   cd server && mvn package
   ```

## 🏷️ 标签系统

### 优先级标签

- 🔴 `priority/critical` - 最高优先级
- 🟠 `priority/high` - 高优先级
- 🟡 `priority/medium` - 中优先级
- 🟢 `priority/low` - 低优先级

### 模块标签

- `module/frontend` - 前端相关
- `module/server` - 服务端相关
- `module/agent` - Agent 相关
- `module/docs` - 文档相关

### 状态标签

- `status/in-progress` - 进行中
- `status/review` - 待审核
- `status/blocked` - 已阻塞

## 📋 Issue 管理

### 创建 Issue

1. 选择合适的模板（Bug/Feature/Nn 任务）
2. 填写完整信息
3. 添加适当的标签
4. 关联 Milestone（如适用）

### Nn 任务

由 Nn 任务系统自动创建的 Issue 会自动：

- 添加 `Nn 任务` 和 `codex` 标签
- 分配给 Codex 执行
- 关联相应 Milestone

## 🔀 Pull Request

### PR 流程

1. 确保代码通过所有 CI 检查
2. 填写 PR 模板
3. 关联相关 Issue
4. 请求 Review
5. 根据反馈修改
6. 合并到主分支

### Code Review

- 至少需要 1 个 Reviewer 批准
- 所有 CI 检查必须通过
- 解决所有 Review 评论

## 📚 文档

### 文档结构

```
docs/
├── 00-overview/        # 总览和导航
├── 01-requirements/    # 需求文档
├── 02-governance/      # 研发规则
├── 03-api/            # API 文档
├── 04-memory/         # 项目记忆
└── 05-skills/         # Skill 说明
```

### 文档更新

- 代码变更需同步更新文档
- 使用清晰的 Markdown 格式
- 保持文档与代码一致

## 🎯 Milestones

项目按 Milestone 组织：

- **M1 - MVP 版本** (2026-03-31)
- **M2 - 批量部署能力** (2026-04-30)
- **M3 - 日志与监控** (2026-05-31)
- **M4 - 插件系统** (2026-06-30)

## ❓ 需要帮助？

- 查看 [文档](docs/)
- 查看现有 Issues
- 在 Discussion 中提问

---

感谢你的贡献！🎉
