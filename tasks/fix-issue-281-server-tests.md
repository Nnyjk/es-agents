# 任务：修复 Issue #281 - Server 测试失败问题

## 背景
PR #276 的 CI 修复完成后，发现 Server 模块存在预存在的测试失败问题。这些测试失败与认证/权限配置相关。

## 失败测试

### 1. CommandExecutionResourceTest.testExecuteCommandValidation
- **期望**: statusCode 400
- **实际**: statusCode 500
- **错误**: `java.lang.IllegalArgumentException: id to load is required for loading`
- **原因**: 测试没有正确配置认证上下文

### 2. AuthResourceTest.testPublicKeyEndpoint
- **期望**: statusCode 200
- **实际**: statusCode 404
- **原因**: 端点路径或认证配置问题

### 3. DeploymentProgressResourceTest 多个测试
- testGetProgressHistoryEmpty - 期望 200 但得到 401
- testGetStatusHistoryEmpty - 期望 200 但得到 401
- testMarkStageCompleteMissingStage - 期望 400 但得到 415
- testMarkStageFailedMissingStage - 期望 400 但得到 401
- **原因**: 认证/权限检查相关问题

## 任务要求

1. **分析测试代码** - 定位每个失败测试的根本原因
2. **修复测试配置** - 添加 Mock 认证或调整测试期望
3. **验证修复** - 运行测试确保通过
4. **提交 PR** - 创建分支并提交修复

## 修复策略

### 方案 A: Mock 认证上下文（推荐）
在测试中添加 Mock 认证，绕过 `@RequiresPermission` 检查

### 方案 B: 调整测试期望
使用 `anyOf(is(200), is(401), is(403))` 接受认证失败场景

### 方案 C: 使用测试专用端点
为测试创建不要求认证的端点变体

## 验收标准
- [ ] CommandExecutionResourceTest.testExecuteCommandValidation 通过
- [ ] AuthResourceTest.testPublicKeyEndpoint 通过
- [ ] DeploymentProgressResourceTest 所有测试通过
- [ ] 其他测试不受影响
- [ ] CI server job 通过

## 文件位置
- 测试文件：`server/src/test/java/com/easystation/*/resource/*Test.java`
- 测试文件：`server/src/test/java/com/easystation/*/service/*Test.java`

## 注意事项
- 使用 esa-runner 用户执行
- 创建分支 `fix/m2-issue-281-server-tests`
- 测试修复不应降低测试质量
- 优先使用 Mock 认证方案
