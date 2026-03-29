# Issue #348: 全局搜索功能开发

## 任务描述
实现统一的全局搜索功能，支持跨模块搜索（主机/部署/配置/日志），提供搜索建议和搜索历史管理。

## 验收标准
- [ ] 搜索响应时间 < 200ms
- [ ] 支持模糊搜索
- [ ] 搜索结果相关性 > 80%
- [ ] 搜索历史可管理（查看/删除/清空）
- [ ] 单元测试覆盖率 > 80%

## 实现要求

### 1. 数据库迁移
创建 `V202603300000__search_history.sql`:
```sql
CREATE TABLE user_search_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    query VARCHAR(500) NOT NULL,
    result_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_search_history_user_id ON user_search_history(user_id);
CREATE INDEX idx_user_search_history_created_at ON user_search_history(created_at);
```

### 2. 领域模型
- `SearchResult.java` - 搜索结果 DTO (type, id, name, description, score, highlights)
- `SearchSuggestion.java` - 搜索建议 DTO (text, type)
- `UserSearchHistory.java` - 搜索历史实体

### 3. Repository 层
- `SearchRepository.java` - 搜索查询逻辑（按类型搜索、聚合统计）
- `SearchHistoryRepository.java` - 搜索历史 CRUD

### 4. Service 层
- `SearchService.java`:
  - `search(query, types, limit, offset)` - 统一搜索
  - `getSuggestions(query, limit)` - 搜索建议
  - `recordSearch(userId, query, resultCount)` - 记录搜索历史
- `SearchHistoryService.java`:
  - `getHistory(userId, limit)` - 获取历史
  - `deleteHistory(userId, historyId)` - 删除单条
  - `clearHistory(userId)` - 清空历史

### 5. Resource 层
- `SearchResource.java`:
  - `GET /api/search?q=&types=&limit=&offset=`
  - `GET /api/search/suggest?q=&limit=`
  - `GET /api/search/history`
  - `POST /api/search/history`
  - `DELETE /api/search/history/{id}`
  - `DELETE /api/search/history`

### 6. 单元测试
- `SearchServiceTest.java`
- `SearchHistoryServiceTest.java`
- `SearchResourceTest.java`

## 文件结构
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

## 注意事项
- 使用 Quarkus Panache 简化数据库操作
- 使用 LIKE 进行模糊搜索
- 搜索结果按相关性评分排序
- 搜索历史保留最近 100 条
- 需要认证授权（@Authenticated）
- 使用 Y-Bot-N 身份提交代码

## 完成标准
1. 所有文件创建完成
2. 本地编译通过 (`mvn compile -q`)
3. 单元测试通过 (`mvn test -q`)
4. 创建 PR 提交
