# #384 插件架构设计 - 实现计划

**Issue**: #384  
**优先级**: P0  
**模块**: plugin-api  
**预计时间**: 2 周

---

## 目标

定义 ESA 插件系统核心架构和 API 规范，包括插件生命周期、元数据格式、接口定义。

---

## 任务分解

### 1. 插件生命周期模型

**状态机**:
```
NEW → INITIALIZING → INITIALIZED → STARTING → ACTIVE → STOPPING → STOPPED
                                    ↓
                                 FAILED
```

**枚举**: `PluginState`
- NEW: 刚创建，未初始化
- INITIALIZING: 初始化中
- INITIALIZED: 已初始化，未启动
- STARTING: 启动中
- ACTIVE: 运行中
- STOPPING: 停止中
- STOPPED: 已停止
- FAILED: 失败

### 2. 插件元数据格式

**文件**: `plugin.json`
```json
{
  "id": "sample-plugin",
  "name": "Sample Plugin",
  "version": "1.0.0",
  "description": "示例插件",
  "author": "Developer",
  "license": "MIT",
  "main": "com.example.plugin.SamplePlugin",
  "dependencies": [
    {"id": "core-plugin", "version": ">=1.0.0"}
  ],
  "provides": ["extension-point-1", "extension-point-2"],
  "requires": ["extension-point-3"],
  "config": {
    "schema": {...},
    "defaults": {...}
  }
}
```

### 3. 核心接口定义

#### Plugin 接口
```java
public interface Plugin {
    /**
     * 初始化插件
     */
    void initialize(PluginContext context);
    
    /**
     * 启动插件
     */
    void start();
    
    /**
     * 停止插件
     */
    void stop();
    
    /**
     * 获取插件描述符
     */
    PluginDescriptor getDescriptor();
    
    /**
     * 获取插件当前状态
     */
    PluginState getState();
}
```

#### PluginDescriptor 类
```java
public class PluginDescriptor {
    private String id;
    private String name;
    private String version;
    private String description;
    private String author;
    private String license;
    private String mainClass;
    private List<Dependency> dependencies;
    private List<String> provides;
    private List<String> requires;
    private PluginConfig config;
}
```

#### PluginContext 接口
```java
public interface PluginContext {
    /**
     * 获取插件数据目录
     */
    Path getDataDirectory();
    
    /**
     * 获取配置
     */
    <T> T getConfig(String key, Class<T> type);
    
    /**
     * 获取服务
     */
    <T> T getService(Class<T> serviceClass);
    
    /**
     * 发布事件
     */
    void publishEvent(PluginEvent event);
    
    /**
     * 记录日志
     */
    Logger getLogger();
}
```

#### ExtensionPoint 接口
```java
public interface ExtensionPoint<T> {
    /**
     * 获取扩展点 ID
     */
    String getId();
    
    /**
     * 获取扩展点名称
     */
    String getName();
    
    /**
     * 获取扩展类型
     */
    Class<T> getExtensionType();
    
    /**
     * 注册扩展
     */
    void register(String pluginId, T extension);
    
    /**
     * 获取所有扩展
     */
    List<T> getExtensions();
}
```

### 4. 插件配置模型

```java
public class PluginConfig {
    private JsonNode schema;  // JSON Schema
    private JsonNode defaults; // 默认值
    
    /**
     * 验证配置
     */
    ValidationResult validate(JsonNode config);
    
    /**
     * 合并默认值和用户配置
     */
    JsonNode merge(JsonNode userConfig);
}
```

---

## 文件结构

```
server/
└── plugin-api/
    └── src/main/java/com/esagents/plugin/
        ├── Plugin.java                    # 插件入口接口
        ├── PluginState.java               # 插件状态枚举
        ├── PluginDescriptor.java          # 插件描述符
        ├── PluginContext.java             # 插件上下文接口
        ├── PluginConfig.java              # 插件配置
        ├── PluginEvent.java               # 插件事件
        ├── extension/
        │   ├── ExtensionPoint.java        # 扩展点接口
        │   └── ExtensionManager.java      # 扩展管理器
        └── event/
            ├── PluginEvent.java           # 事件基类
            ├── PluginInitializedEvent.java
            ├── PluginStartedEvent.java
            └── PluginStoppedEvent.java
```

---

## 验收标准

- [ ] Plugin 接口定义完整
- [ ] PluginState 状态机清晰
- [ ] PluginDescriptor 包含所有必要字段
- [ ] PluginContext 提供必要服务
- [ ] ExtensionPoint 机制设计完成
- [ ] 编写单元测试
- [ ] 编写 API 文档

---

## 下一步

完成 #384 后:
1. 创建 #385 实现计划
2. 实现 PluginLoader
3. 实现 PluginManager
