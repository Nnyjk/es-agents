# 插件架构设计文档

**Issue**: #384  
**模块**: plugin-api  
**版本**: 1.0.0  
**日期**: 2026-03-30

---

## 1. 概述

ESA 插件系统提供可扩展、隔离、热插拔的插件架构，允许开发者通过插件扩展 ESA 平台功能。

### 1.1 设计目标

- **可扩展性**: 支持第三方开发者创建和发布插件
- **隔离性**: 插件运行在独立沙箱中，互不影响
- **热插拔**: 支持插件动态加载、卸载，无需重启
- **类型安全**: 基于 Java 接口定义，编译时类型检查
- **版本兼容**: 支持语义化版本和依赖管理

### 1.2 核心概念

| 概念 | 说明 |
|------|------|
| **Plugin** | 插件实例，实现生命周期管理 |
| **PluginDescriptor** | 插件元数据，从 plugin.json 加载 |
| **PluginContext** | 插件上下文，提供宿主环境访问 |
| **ExtensionPoint** | 扩展点，定义可扩展的功能点 |
| **PluginLoader** | 插件加载器，负责加载和管理插件 |

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    ESA Host Application                  │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │   Plugin    │  │   Plugin    │  │   Plugin    │     │
│  │     A       │  │     B       │  │     C       │     │
│  │  ┌───────┐  │  │  ┌───────┐  │  │  ┌───────┐  │     │
│  │  │Extension│ │  │  │Extension│ │  │  │Extension│ │     │
│  │  └───────┘  │  │  └───────┘  │  │  └───────┘  │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
│         │                │                │             │
│         └────────────────┼────────────────┘             │
│                          │                              │
│              ┌───────────▼───────────┐                 │
│              │    PluginRegistry     │                 │
│              └───────────┬───────────┘                 │
│                          │                              │
│              ┌───────────▼───────────┐                 │
│              │     PluginLoader      │                 │
│              └───────────┬───────────┘                 │
│                          │                              │
│              ┌───────────▼───────────┐                 │
│              │    ExtensionRegistry  │                 │
│              └───────────────────────┘                 │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 PluginLoader

负责插件的加载、初始化、启动、停止和卸载。

**职责**:
- 扫描插件目录
- 加载插件类
- 管理插件生命周期
- 处理依赖解析

#### 2.2.2 PluginRegistry

维护已加载插件的注册表。

**职责**:
- 存储插件实例
- 提供插件查询
- 管理插件状态

#### 2.2.3 ExtensionRegistry

管理扩展点的注册和发现。

**职责**:
- 注册扩展点
- 注册扩展实现
- 提供扩展查询

#### 2.2.4 PluginContext

提供插件访问宿主环境的接口。

**职责**:
- 提供配置访问
- 提供服务查找
- 提供扩展注册
- 提供事件发布

---

## 3. 插件生命周期

### 3.1 状态机

```
                          ┌──────────┐
                          │   NEW    │
                          └────┬─────┘
                               │ load()
                               ▼
                    ┌──────────────────┐
                    │  INITIALIZING    │
                    └────────┬─────────┘
                             │ initialize()
                             ▼
                    ┌──────────────────┐
                    │  INITIALIZED     │
                    └────────┬─────────┘
                             │ start()
                             ▼
                    ┌──────────────────┐
                    │    STARTING      │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │     ACTIVE       │◄────┐
                    └────────┬─────────┘     │
                             │               │
                    ┌────────┴────────┐     │
                    │                 │     │
                    ▼                 ▼     │
           ┌──────────────┐    ┌───────────┴┐
           │   STOPPING   │    │   FAILED   │
           └──────┬───────┘    └────────────┘
                  │ stop()
                  ▼
           ┌──────────────┐
           │   STOPPED    │
           └──────────────┘
```

### 3.2 生命周期方法

| 方法 | 状态转换 | 说明 |
|------|----------|------|
| `load()` | NEW → INITIALIZING | 加载插件类和资源 |
| `initialize(ctx)` | INITIALIZING → INITIALIZED | 初始化插件，注册扩展点 |
| `start()` | INITIALIZED → STARTING → ACTIVE | 启动插件服务 |
| `stop()` | ACTIVE → STOPPING → STOPPED | 停止插件，释放资源 |
| `unload()` | STOPPED → NEW | 卸载插件（可选） |

---

## 4. 扩展点机制

### 4.1 扩展点定义

```java
public interface WebhookHandler extends ExtensionPoint<WebhookHandler> {
    void handle(WebhookEvent event);
}
```

### 4.2 扩展实现

```java
@Extension("github-webhook")
public class GitHubWebhookHandler implements WebhookHandler {
    @Override
    public void handle(WebhookEvent event) {
        // 处理 GitHub Webhook
    }
}
```

### 4.3 扩展发现

```java
// 获取所有 Webhook 处理器
List<WebhookHandler> handlers = context.getExtensions("webhook-handler");

// 遍历调用
for (WebhookHandler handler : handlers) {
    handler.handle(event);
}
```

---

## 5. 依赖管理

### 5.1 依赖解析顺序

1. 检查依赖插件是否已加载
2. 如果未加载，尝试加载依赖插件
3. 检查版本兼容性
4. 建立依赖关系图

### 5.2 循环依赖检测

使用拓扑排序检测循环依赖：

```
A → B → C → A  (循环依赖，拒绝加载)
A → B → C      (有效依赖，按 C→B→A 顺序加载)
```

---

## 6. 安全隔离

### 6.1 类加载隔离

每个插件使用独立的 ClassLoader：

```
Bootstrap ClassLoader
    └── System ClassLoader (Host)
        └── PluginClassLoader A
        └── PluginClassLoader B
        └── PluginClassLoader C
```

### 6.2 权限控制

插件声明所需权限，运行时检查：

```json
{
  "permissions": [
    "network:outbound",
    "storage:persistent"
  ]
}
```

### 6.3 资源限制

- CPU 使用限制
- 内存使用限制
- 线程数限制
- I/O 操作限制

---

## 7. 配置管理

### 7.1 配置层次

```
系统配置 → 插件默认配置 → 用户配置 → 环境变量
```

### 7.2 配置 Schema

使用 JSON Schema 定义配置结构，提供类型验证和 IDE 支持。

---

## 8. 事件机制

### 8.1 事件类型

| 事件 | 说明 |
|------|------|
| `PluginLoadedEvent` | 插件加载完成 |
| `PluginStartedEvent` | 插件启动完成 |
| `PluginStoppedEvent` | 插件停止完成 |
| `PluginFailedEvent` | 插件失败 |
| `ExtensionRegisteredEvent` | 扩展注册 |
| `ExtensionUnregisteredEvent` | 扩展注销 |

### 8.2 事件订阅

```java
context.subscribe(PluginStartedEvent.class, event -> {
    log.info("Plugin started: " + event.getPluginId());
});
```

---

## 9. 错误处理

### 9.1 异常类型

| 异常 | 说明 |
|------|------|
| `PluginException` | 插件操作异常 |
| `PluginLoadException` | 插件加载失败 |
| `PluginInitException` | 插件初始化失败 |
| `PluginStartException` | 插件启动失败 |
| `DependencyException` | 依赖解析失败 |
| `VersionConflictException` | 版本冲突 |

### 9.2 错误恢复

- 初始化失败：标记为 FAILED，可重试
- 启动失败：标记为 FAILED，可重试
- 运行时错误：记录日志，继续运行

---

## 10. 目录结构

```
<plugin-root>/
├── plugin.json              # 插件元数据
├── META-INF/
│   └── plugin/
│       ├── extensions.xml   # 扩展点定义
│       └── resources/       # 插件资源
├── lib/
│   └── *.jar                # 依赖库
└── classes/
    └── com/example/
        └── MyPlugin.class   # 插件类
```

---

## 11. 开发指南

### 11.1 创建插件

1. 创建插件项目
2. 实现 `Plugin` 接口
3. 创建 `plugin.json`
4. 打包插件

### 11.2 测试插件

1. 单元测试
2. 集成测试
3. 沙箱测试

### 11.3 发布插件

1. 提交到插件市场
2. 等待审核
3. 发布

---

## 12. 相关文件

- [Plugin 接口](./Plugin.java)
- [PluginDescriptor 接口](./PluginDescriptor.java)
- [PluginContext 接口](./PluginContext.java)
- [ExtensionPoint 接口](./ExtensionPoint.java)
- [PluginState 枚举](./PluginState.java)
- [PluginException 异常](./PluginException.java)
- [plugin.json 规范](./PLUGIN_JSON_SPEC.md)

---

## 13. 后续工作

- [ ] 实现 PluginLoader
- [ ] 实现 PluginRegistry
- [ ] 实现 ExtensionRegistry
- [ ] 实现插件沙箱
- [ ] 实现插件市场
