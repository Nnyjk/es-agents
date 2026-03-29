# M5 Phase 3: #348 全局搜索功能实现计划

## 需求分析

### 核心功能
1. **统一搜索 API** - `/api/search` 端点
2. **搜索结果聚合** - 主机/部署/配置/日志
3. **搜索建议** - 自动补全 `/api/search/suggest`
4. **搜索历史** - 用户搜索记录管理
5. **高级搜索** - 多条件过滤

### 验收标准
- 搜索响应时间 < 200ms
- 支持模糊搜索
- 搜索结果相关性 > 80%
- 搜索历史可管理

## 技术方案

### 数据库设计
```sql
-- 搜索历史表
CREATE TABLE user_search_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    query VARCHAR(500) NOT NULL,
    result_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_user_search_history_user_id ON user_search_history(user_id);
CREATE INDEX idx_user_search_history_created_at ON user_search_history(created_at);
```

### API 设计

#### 1. 统一搜索 API
```
GET /api/search
Query Params:
  - q: 搜索关键词（必填）
  - types: 搜索类型，逗号分隔（host,deployment,config,log）
  - limit: 结果数量限制（默认 20）
  - offset: 偏移量（默认 0）

Response:
{
  "query": "keyword",
  "total": 100,
  "results": [
    {
      "type": "host",
      "id": "uuid",
      "name": "主机名",
      "description": "描述",
      "score": 0.95,
      "highlights": ["高亮文本"]
    }
  ],
  "aggregations": {
    "host": 50,
    "deployment": 30,
    "config": 15,
    "log": 5
  }
}
```

#### 2. 搜索建议 API
```
GET /api/search/suggest
Query Params:
  - q: 搜索关键词（必填）
  - limit: 建议数量（默认 5）

Response:
{
  "suggestions": [
    {"text": "keyword1", "type": "host"},
    {"text": "keyword2", "type": "deployment"}
  ]
}
```

#### 3. 搜索历史 API
```
GET /api/search/history - 获取搜索历史
POST /api/search/history - 记录搜索
DELETE /api/search/history/{id} - 删除单条历史
DELETE /api/search/history - 清空历史
```

## 实现步骤

### Step 1: 数据库迁移
- 创建 `V202603300000__search_history.sql`
- 添加索引优化查询性能

### Step 2: 领域模型
- `SearchResult.java` - 搜索结果 DTO
- `SearchSuggestion.java` - 搜索建议 DTO
- `UserSearchHistory.java` - 搜索历史实体

### Step 3: Repository 层
- `SearchRepository.java` - 搜索查询逻辑
- `SearchHistoryRepository.java` - 搜索历史 CRUD

### Step 4: Service 层
- `SearchService.java` - 核心搜索服务
  - `search(query, types, limit, offset)` - 统一搜索
  - `getSuggestions(query, limit)` - 搜索建议
  - `recordSearch(userId, query)` - 记录搜索历史
- `SearchHistoryService.java` - 搜索历史管理

### Step 5: Resource 层
- `SearchResource.java` - REST API 端点
  - `GET /api/search`
  - `GET /api/search/suggest`
  - `GET/POST/DELETE /api/search/history`

### Step 6: 单元测试
- `SearchServiceTest.java`
- `SearchResourceTest.java`

## 文件清单

```
server/src/main/java/com/easystation/search/
├── domain/
│   ├── SearchResult.java
│   ├── SearchSuggestion.java
│   └── UserSearchHistory.java
├── repository/
│   ├── SearchRepository.java
│   └── SearchHistoryRepository.java
├── service/
│   ├── SearchService.java
│   └── SearchHistoryService.java
└── resource/
    └── SearchResource.java

server/src/main/resources/db/migration/
└── V202603300000__search_history.sql

server/src/test/java/com/easystation/search/
├── service/
│   ├── SearchServiceTest.java
│   └── SearchHistoryServiceTest.java
└── resource/
    └── SearchResourceTest.java
```

## 验收检查

- [ ] 本地编译通过
- [ ] 单元测试通过
- [ ] API 响应时间 < 200ms
- [ ] 支持模糊搜索
- [ ] 搜索结果包含相关性评分
- [ ] 搜索历史功能完整

## 备注

- 使用 Quarkus Panache 简化数据库操作
- 使用 LIKE 进行模糊搜索（后续可优化为全文索引）
- 搜索结果按相关性排序
- 搜索历史保留最近 100 条
