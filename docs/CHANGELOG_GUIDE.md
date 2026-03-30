# CHANGELOG 自动生成指南

本文档说明 ES Agents 项目的 CHANGELOG 自动生成流程和版本发布规范。

## 概述

本项目使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范自动生成 CHANGELOG 和版本发布说明。

## Conventional Commits 规范

### Commit 格式

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Commit 类型

| 类型       | 说明                       | 是否出现在 CHANGELOG        |
| ---------- | -------------------------- | --------------------------- |
| `feat`     | 新功能                     | ✅ Features                 |
| `fix`      | Bug 修复                   | ✅ Bug Fixes                |
| `docs`     | 文档更新                   | ✅ Documentation            |
| `style`    | 代码格式（不影响代码逻辑） | ❌                          |
| `refactor` | 代码重构                   | ✅ Code Refactoring         |
| `perf`     | 性能优化                   | ✅ Performance Improvements |
| `test`     | 测试相关                   | ❌                          |
| `build`    | 构建系统                   | ✅ Build System             |
| `ci`       | CI/CD 配置                 | ✅ Continuous Integration   |
| `chore`    | 其他杂项                   | ❌                          |
| `revert`   | 回滚提交                   | ✅ Reverts                  |

### 示例

```bash
# 新功能
git commit -m "feat(auth): add OAuth2 authentication support"

# Bug 修复
git commit -m "fix(deployment): resolve connection timeout issue"

# 文档更新
git commit -m "docs(readme): update installation instructions"

# 重构
git commit -m "refactor(agent): simplify plugin loading logic"

# 带 Breaking Change
git commit -m "feat(api)!: change authentication endpoint format

BREAKING CHANGE: authentication endpoint now requires Bearer token prefix"
```

## 版本发布流程

### 1. 自动更新 CHANGELOG

当 PR 合并到 `main` 分支时，GitHub Action 会自动：

- 解析 Conventional Commits
- 更新 `CHANGELOG.md`
- 提交变更

### 2. 手动发布版本

通过 GitHub Actions 手动触发：

1. 进入 **Actions** → **Release**
2. 点击 **Run workflow**
3. 输入版本号（如 `v1.0.0`）
4. 点击 **Run workflow**

发布流程会自动：

- 构建前端、服务端、Agent
- 生成对应版本的 CHANGELOG 段落
- 创建 GitHub Release 并上传构建产物

## 文件说明

- `CHANGELOG.md` - 项目变更日志
- `.versionrc.json` - conventional-changelog 配置
- `.github/workflows/changelog.yml` - CHANGELOG 自动更新工作流
- `.github/workflows/release.yml` - 版本发布工作流

## 最佳实践

1. **及时提交**：每个功能/修复单独提交，使用规范的 commit message
2. **明确 scope**：在 commit message 中指明影响范围（如 `feat(auth)`, `fix(deployment)`）
3. **Breaking Change**：使用 `!` 标记或在 footer 中写明 `BREAKING CHANGE`
4. **版本语义化**：遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)
   - MAJOR: 不兼容的 API 变更
   - MINOR: 向后兼容的功能性新增
   - PATCH: 向后兼容的问题修复

## 工具安装

本地生成 CHANGELOG：

```bash
# 安装 conventional-changelog-cli
npm install -g conventional-changelog-cli

# 生成 CHANGELOG
conventional-changelog -p angular -i CHANGELOG.md -s -r 0
```

## 相关链接

- [Conventional Commits 规范](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [conventional-changelog-cli](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-cli)
