# 修复 PR #402 CI 失败

## 背景
PR #402 (fix/main-compile-errors-401) CI 失败，有 3 个 Job 失败：

1. **Server 测试失败**：
   - `PluginMarketplaceTest.testUninstallPlugin` - NPE 因为 pluginManager 是 null
   - `CollaborationFrameworkTest.testTaskLifecycle` - BuildException: Unsatisfied dependency for PluginManager

2. **Frontend 测试失败**：
   - vitest 版本兼容问题：`BaseCoverageProvider` 导出错误

3. **Docs 检查失败**：
   - 13 个错误，28 个警告，文档缺少标题

## 修复任务

### Task 1: 修复 PluginMarketplaceTest
文件：`server/src/test/java/com/easystation/plugin/marketplace/PluginMarketplaceTest.java`

问题：测试使用 `new PluginMarketplaceImpl()`，但 `pluginManager` 字段通过 `@Inject` 注入，普通单元测试中是 null。

修复方案：
1. 给测试类添加 `@QuarkusTest` 注解
2. 注入 `PluginMarketplace` 而不是直接实例化
3. 或者使用 `Mockito` mock PluginManager

推荐方案：使用 `@QuarkusTest` + `@Inject`：
```java
@QuarkusTest
class PluginMarketplaceTest {
    @Inject
    PluginMarketplace marketplace;
    
    // 移除 @BeforeEach setUp()
}
```

### Task 2: 修复 CollaborationFrameworkTest
文件：`server/src/test/java/com/easystation/agent/collaboration/CollaborationFrameworkTest.java`

问题：Quarkus 测试中 PluginManager bean 无法解析

根因：`PluginManagerProducer` 依赖 `plugin.directory` 配置，测试环境可能没有正确配置。

修复方案：
1. 添加测试配置文件 `server/src/test/resources/application-test.properties`
2. 添加 `plugin.directory=plugins` 配置
3. 或者在测试中 mock PluginManager

### Task 3: 检查 Frontend vitest 配置
目录：`frontend/`

检查 vitest 版本和配置，修复 `BaseCoverageProvider` 导出问题。

可能需要调整：
- `frontend/package.json` 中 vitest 版本
- `frontend/vitest.config.ts` 配置

### Task 4: 检查 Docs 脚本（可选）
目录：`docs/` 和 `.github/workflows/`

文档检查脚本可能需要调整容错级别或修复缺失标题的文档。

## 验收标准
- `mvn test -f server/pom.xml` 通过
- `npm run test:coverage` 在 frontend 通过
- Docs 检查通过（或至少不报错）

## 执行要求
1. 分析每个失败的根本原因
2. 逐一修复，不要跳过
3. 每个修复后验证本地测试
4. 提交时使用消息: `fix: 修复 CI 测试失败 (PR #402)`
