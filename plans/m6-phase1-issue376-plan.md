# M6 Phase 1 - Issue #376 实现计划

## 目标
构建 Agent 工具调用框架，支持动态加载和调用各种工具/技能。

## 功能范围
1. 工具注册与发现
2. 工具调用协议
3. 参数验证与转换
4. 执行结果处理
5. 工具权限控制

## 技术设计

### 1. 数据模型

#### ToolDefinition（工具定义）
```java
@Entity
@Table(name = "agent_tool_definition")
public class ToolDefinition {
    @Id
    private String id;              // 工具唯一标识
    private String name;            // 工具名称
    private String description;     // 工具描述
    private String category;        // 工具分类
    private String version;         // 版本号
    private ToolStatus status;      // 状态 (ENABLED, DISABLED, DEPRECATED)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### ToolParameter（工具参数）
```java
@Entity
@Table(name = "agent_tool_parameter")
public class ToolParameter {
    @Id
    private String id;
    @ManyToOne
    private ToolDefinition tool;    // 所属工具
    private String name;            // 参数名
    private String type;            // 参数类型 (string, number, boolean, object, array)
    private String description;     // 参数描述
    private boolean required;       // 是否必填
    private String defaultValue;    // 默认值
    private String validationRule;  // 验证规则 (JSON Schema)
    private int order;              // 参数顺序
}
```

#### ToolExecutionLog（工具执行日志）
```java
@Entity
@Table(name = "agent_tool_execution_log")
public class ToolExecutionLog {
    @Id
    private String id;
    private String toolId;          // 工具 ID
    private String taskId;          // 关联任务 ID
    private String input;           // 输入参数 (JSON)
    private String output;          // 输出结果 (JSON)
    private String error;           // 错误信息
    private ToolExecutionStatus status; // 状态
    private long durationMs;        // 执行耗时
    private LocalDateTime executedAt; // 执行时间
}
```

### 2. 枚举定义

#### ToolStatus
```java
public enum ToolStatus {
    ENABLED,    // 已启用
    DISABLED,   // 已禁用
    DEPRECATED  // 已废弃
}
```

#### ToolExecutionStatus
```java
public enum ToolExecutionStatus {
    PENDING,    // 等待执行
    RUNNING,    // 执行中
    SUCCESS,    // 执行成功
    FAILED,     // 执行失败
    TIMEOUT     // 执行超时
}
```

### 3. 核心接口

#### Tool（工具接口）
```java
public interface Tool {
    String getId();                       // 工具 ID
    String getName();                     // 工具名称
    String getDescription();              // 工具描述
    List<ToolParameter> getParameters();  // 参数定义
    ToolExecutionResult execute(Map<String, Object> params); // 执行方法
}
```

#### ToolRegistry（工具注册表）
```java
public interface ToolRegistry {
    void register(Tool tool);             // 注册工具
    void unregister(String toolId);       // 注销工具
    Tool getTool(String toolId);          // 获取工具
    List<Tool> listTools();               // 列出所有工具
    List<Tool> searchTools(String query); // 搜索工具
}
```

#### ToolExecutor（工具执行器）
```java
public interface ToolExecutor {
    ToolExecutionResult execute(String toolId, Map<String, Object> params);
    ToolExecutionResult executeAsync(String toolId, Map<String, Object> params);
    void cancelExecution(String executionId);
}
```

### 4. 工具调用协议

#### 请求格式
```json
{
  "toolId": "shell.execute",
  "parameters": {
    "command": "ls -la",
    "timeout": 30000,
    "workingDir": "/tmp"
  },
  "context": {
    "taskId": "task-123",
    "userId": "user-456"
  }
}
```

#### 响应格式
```json
{
  "executionId": "exec-789",
  "status": "SUCCESS",
  "output": {
    "stdout": "...",
    "stderr": "",
    "exitCode": 0
  },
  "durationMs": 1234,
  "executedAt": "2026-03-30T10:00:00Z"
}
```

### 5. 内置工具

#### Shell 工具
- `shell.execute` - 执行 Shell 命令
- `shell.read_file` - 读取文件
- `shell.write_file` - 写入文件

#### HTTP 工具
- `http.get` - GET 请求
- `http.post` - POST 请求
- `http.put` - PUT 请求
- `http.delete` - DELETE 请求

#### 文件工具
- `file.list` - 列出目录
- `file.create` - 创建文件/目录
- `file.delete` - 删除文件/目录
- `file.copy` - 复制文件/目录
- `file.move` - 移动文件/目录

## 实现步骤

### Step 1: 数据模型定义
- ToolDefinition 实体
- ToolParameter 实体
- ToolExecutionLog 实体
- ToolStatus 枚举
- ToolExecutionStatus 枚举
- Repository 接口

### Step 2: 核心接口定义
- Tool 接口
- ToolRegistry 接口
- ToolExecutor 接口
- ToolExecutionResult DTO

### Step 3: 工具注册表实现
- ToolRegistryImpl（基于内存 + 数据库持久化）
- 工具发现机制（SPI 扫描）
- 工具分类索引

### Step 4: 工具执行器实现
- ToolExecutorImpl
- 参数验证
- 超时控制
- 异步执行支持

### Step 5: 内置工具实现
- ShellTools（shell.execute, shell.read_file, shell.write_file）
- HttpTools（http.get, http.post, http.put, http.delete）
- FileTools（file.list, file.create, file.delete, file.copy, file.move）

### Step 6: REST API 实现
- ToolResource（工具管理端点）
- ToolExecutionResource（工具执行端点）
- DTO 转换

### Step 7: 测试验证
- 单元测试（30+ 测试用例）
- 集成测试

## 验收标准
- [ ] 支持动态加载工具
- [ ] 工具调用接口统一
- [ ] 参数自动验证
- [ ] 支持异步调用
- [ ] 工具调用日志记录
- [ ] 内置工具可用（Shell, HTTP, File）
- [ ] REST API 完整

## 依赖
- #375 Agent 任务规划能力（已完成，PR #380 审查中）

## 文件结构
```
server/src/main/java/com/easystation/agent/tool/
├── domain/
│   ├── ToolDefinition.java
│   ├── ToolParameter.java
│   ├── ToolExecutionLog.java
│   ├── ToolStatus.java
│   └── ToolExecutionStatus.java
├── repository/
│   ├── ToolDefinitionRepository.java
│   ├── ToolParameterRepository.java
│   └── ToolExecutionLogRepository.java
├── spi/
│   ├── Tool.java
│   ├── ToolRegistry.java
│   ├── ToolExecutor.java
│   └── ToolExecutionResult.java
├── impl/
│   ├── ToolRegistryImpl.java
│   └── ToolExecutorImpl.java
├── builtin/
│   ├── shell/
│   │   ├── ShellTools.java
│   │   └── ShellToolProvider.java
│   ├── http/
│   │   ├── HttpTools.java
│   │   └── HttpToolProvider.java
│   └── file/
│       ├── FileTools.java
│       └── FileToolProvider.java
├── dto/
│   ├── ToolDefinitionDTO.java
│   ├── ToolExecutionRequest.java
│   └── ToolExecutionResponse.java
├── resource/
│   ├── ToolResource.java
│   └── ToolExecutionResource.java
└── validator/
    └── ToolParameterValidator.java
```

## 时间估算
- Step 1: 30 分钟
- Step 2: 20 分钟
- Step 3: 40 分钟
- Step 4: 40 分钟
- Step 5: 60 分钟
- Step 6: 30 分钟
- Step 7: 30 分钟
- **总计**: 约 4 小时
