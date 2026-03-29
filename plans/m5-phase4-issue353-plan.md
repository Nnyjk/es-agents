# M5 Phase 4 - Issue #353: 配置热重载

## 目标

实现配置热重载功能，无需重启服务即可更新配置。

## 功能范围

1. **配置变更监听**
   - 数据库配置表监听
   - Redis 配置发布/订阅
   - 配置文件监控（可选）

2. **配置热更新 API**
   - PUT `/api/v1/config/{key}` - 更新配置
   - GET `/api/v1/config/{key}` - 获取配置
   - GET `/api/v1/config` - 获取所有配置

3. **配置版本管理**
   - 配置变更历史记录
   - 配置版本回滚

4. **配置变更审计**
   - 记录变更人和时间
   - 记录变更前后值

## 技术方案

### 方案 A: 数据库 + Redis Pub/Sub

**架构**:
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Admin     │────▶│  Config API  │────▶│  Database   │
│   Client    │     │   (Server)   │     │  (config_t) │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │    Redis     │
                    │   Pub/Sub    │
                    └──────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   Server     │
                    │  (Listener)  │
                    └──────────────┘
```

**流程**:
1. 管理员通过 API 更新配置
2. 配置保存到数据库
3. 发布 Redis 消息通知配置变更
4. Server 监听 Redis 消息，刷新本地缓存

### 方案 B: Quarkus Config Sources

使用 Quarkus 动态配置源，但 Quarkus 原生不支持热重载。

### 推荐方案：方案 A

**理由**:
- 灵活可控
- 支持审计和版本管理
- 与现有 Redis 集成一致

## 数据模型

### Config 表

```sql
CREATE TABLE config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    version INT DEFAULT 1,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### ConfigHistory 表

```sql
CREATE TABLE config_history (
    id BIGSERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES config(id)
);
```

## 实施步骤

### Step 1: 数据库迁移

- [ ] 创建 Flyway 迁移脚本 `V1.15__create_config_tables.sql`
- [ ] 创建 `config` 表
- [ ] 创建 `config_history` 表

### Step 2: 创建实体类

- [ ] `Config.java` - 配置实体
- [ ] `ConfigHistory.java` - 配置历史实体

### Step 3: 创建 DTOs

- [ ] `ConfigDTO.java` - 配置数据传输对象
- [ ] `ConfigUpdateRequest.java` - 更新请求
- [ ] `ConfigHistoryDTO.java` - 历史 DTO

### Step 4: 创建 Service

- [ ] `ConfigService.java` - 配置服务
- [ ] `ConfigCache.java` - 配置缓存（Redis）

### Step 5: 创建 REST API

- [ ] `ConfigResource.java` - 配置管理 API
  - GET `/api/v1/config` - 获取所有配置
  - GET `/api/v1/config/{key}` - 获取单个配置
  - PUT `/api/v1/config/{key}` - 更新配置
  - GET `/api/v1/config/{key}/history` - 获取变更历史
  - POST `/api/v1/config/{key}/rollback` - 回滚到指定版本

### Step 6: Redis Pub/Sub 集成

- [ ] `ConfigChangeListener.java` - 监听配置变更
- [ ] 发布配置变更消息

### Step 7: 测试验证

- [ ] 编译测试
- [ ] API 测试
- [ ] 热重载测试

## 文件结构

```
server/src/main/java/com/easystation/config/
├── entity/
│   ├── Config.java
│   └── ConfigHistory.java
├── dto/
│   ├── ConfigDTO.java
│   ├── ConfigUpdateRequest.java
│   └── ConfigHistoryDTO.java
├── service/
│   ├── ConfigService.java
│   └── ConfigCache.java
├── resource/
│   └── ConfigResource.java
└── listener/
    └── ConfigChangeListener.java

server/src/main/resources/db/migration/
└── V1.15__create_config_tables.sql
```

## 验收标准

- [ ] 编译成功 (`mvn compile`)
- [ ] 配置生效时间 < 5s
- [ ] 无需重启服务
- [ ] 配置变更可追溯
- [ ] 支持回滚操作
- [ ] PR 创建并推送

## 依赖

- Quarkus Redis Client
- Quarkus REST (JAX-RS)
- Flyway 数据库迁移
- CDI 事件总线

## 备注

- 运维效率提升功能
- 支持动态调整系统参数
- 需要权限控制（仅管理员可修改）
