# M6 Phase 1 - Issue #377 实现计划

## Agent 记忆与上下文管理

**Issue**: #377  
**优先级**: P1 (Medium)  
**估计工时**: 2-3 天  
**依赖**: 无

---

## 目标

实现 Agent 记忆与上下文管理能力，支持：
- 短期记忆（对话上下文）
- 长期记忆（向量数据库）
- 记忆检索与关联
- 上下文窗口管理
- 记忆压缩与摘要

---

## 技术方案

### 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    Agent Memory System                   │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Short-Term  │  │ Long-Term   │  │  Context    │     │
│  │  Memory     │  │  Memory     │  │  Manager    │     │
│  │  (Cache)    │  │  (Vector)   │  │             │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│         │                │                │             │
│         └────────────────┼────────────────┘             │
│                          │                              │
│              ┌───────────┴───────────┐                 │
│              │    Memory Service     │                 │
│              │    (REST API)         │                 │
│              └───────────────────────┘                 │
└─────────────────────────────────────────────────────────┘
```

### 数据模型

#### 1. 记忆实体 (Memory)

```java
@Entity
public class Memory {
    UUID id;
    String sessionId;        // 会话 ID
    String type;             // SHORT_TERM / LONG_TERM
    String content;          // 记忆内容
    Map<String, Object> metadata;  // 元数据
    LocalDateTime createdAt;
    LocalDateTime expiresAt; // 过期时间（短期记忆）
    int accessCount;         // 访问次数
    double importance;       // 重要性评分
}
```

#### 2. 会话实体 (Session)

```java
@Entity
public class Session {
    UUID id;
    String sessionId;
    String agentId;
    String userId;
    LocalDateTime createdAt;
    LocalDateTime lastAccessedAt;
    Map<String, String> context;  // 上下文变量
}
```

#### 3. 记忆索引 (MemoryIndex) - 用于向量检索

```java
@Entity
public class MemoryIndex {
    UUID id;
    UUID memoryId;
    float[] embedding;       // 向量嵌入
    String text;             // 原始文本
}
```

### 核心接口

#### MemoryService

```java
public interface MemoryService {
    // 短期记忆
    void addShortTermMemory(String sessionId, String content);
    List<Memory> getShortTermMemories(String sessionId, int limit);
    void clearShortTermMemories(String sessionId);
    
    // 长期记忆
    void addLongTermMemory(String sessionId, String content, Map<String, Object> metadata);
    List<Memory> searchLongTermMemories(String query, int limit);
    void deleteLongTermMemory(UUID memoryId);
    
    // 上下文管理
    void setContextVariable(String sessionId, String key, String value);
    String getContextVariable(String sessionId, String key);
    Map<String, String> getAllContextVariables(String sessionId);
    
    // 记忆压缩
    String compressMemories(List<Memory> memories);
}
```

### 实现步骤

#### Step 1: 数据模型定义
- [ ] Memory 实体
- [ ] Session 实体
- [ ] MemoryIndex 实体
- [ ] 枚举：MemoryType, MemoryImportance
- [ ] Repository 接口 (3 个)

#### Step 2: 短期记忆实现
- [ ] ShortTermMemoryManager (基于 Caffeine/Guava Cache)
- [ ] 过期策略
- [ ] LRU 淘汰

#### Step 3: 长期记忆实现
- [ ] LongTermMemoryManager
- [ ] 向量嵌入生成 (集成 embedding 模型)
- [ ] 相似度搜索

#### Step 4: 上下文管理器
- [ ] ContextManager
- [ ] 上下文变量存储
- [ ] 上下文窗口管理

#### Step 5: 记忆压缩与摘要
- [ ] MemoryCompressor
- [ ] 基于 LLM 的摘要生成
- [ ] 重要记忆保留策略

#### Step 6: REST API
- [ ] MemoryResource
  - POST /api/agent/memories
  - GET /api/agent/memories/session/{sessionId}
  - GET /api/agent/memories/search
  - DELETE /api/agent/memories/{id}
  - POST /api/agent/memories/compress
  - GET /api/agent/context/{sessionId}
  - PUT /api/agent/context/{sessionId}

#### Step 7: 测试
- [ ] 单元测试 (30+)
- [ ] 集成测试
- [ ] API 测试

---

## 技术栈

- **缓存**: Caffeine (短期记忆)
- **向量数据库**: PostgreSQL pgvector 或 内存向量索引
- **嵌入模型**: 本地轻量模型或 API
- **Quarkus**: Panache, RESTEasy Reactive

---

## 验收标准

- [ ] 支持多轮对话上下文
- [ ] 长期记忆可检索
- [ ] 记忆关联准确
- [ ] 上下文窗口可配置
- [ ] 支持记忆导出/导入
- [ ] 单元测试覆盖核心逻辑

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 向量搜索性能 | 高 | 使用索引，限制搜索范围 |
| 内存占用 | 中 | LRU 淘汰，定期清理 |
| Embedding 质量 | 中 | 选择合适模型，测试验证 |

---

## 时间估算

| 步骤 | 工时 |
|------|------|
| Step 1: 数据模型 | 2h |
| Step 2: 短期记忆 | 3h |
| Step 3: 长期记忆 | 4h |
| Step 4: 上下文管理 | 2h |
| Step 5: 记忆压缩 | 3h |
| Step 6: REST API | 3h |
| Step 7: 测试 | 4h |
| **总计** | **21h** (~3 天) |

---

## 相关文件

- `server/src/main/java/com/easystation/agent/memory/` - 记忆模块代码
- `server/src/test/java/com/easystation/agent/memory/` - 测试代码
- `plans/m6-phase1-issue377-plan.md` - 本计划文件
