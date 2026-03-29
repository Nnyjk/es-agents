# M5 Phase 4 #354 日志轮转与归档实现计划

## 目标
实现日志轮转与归档功能，防止日志文件过大，支持日志归档和清理。

## 需求分析

### 功能需求
1. **日志轮转**：按大小或时间自动轮转日志文件
2. **日志归档**：旧日志文件压缩归档
3. **日志清理**：自动删除过期归档日志
4. **配置支持**：通过配置文件控制轮转策略

### 技术选型
- **Quarkus Logging**：使用 Quarkus 内置日志系统
2. **Logback**：Quarkus 默认日志后端，支持轮转策略
3. **application.properties**：配置轮转参数

## 实现步骤

### 1. 配置日志轮转策略
**文件**: `server/src/main/resources/application.properties`

```properties
# 日志轮转配置
quarkus.log.file.rotation.max-file-size=100M
quarkus.log.file.rotation.max-backup-index=10
quarkus.log.file.rotation.file-suffix=.yyyy-MM-dd-HH-mm
quarkus.log.file.rotation.compress=true
```

### 2. 创建日志管理配置类
**文件**: `server/src/main/java/com/easystation/logging/LogRotationConfig.java`

- 日志轮转配置属性
- 归档目录配置
- 保留天数配置

### 3. 创建日志管理服务
**文件**: `server/src/main/java/com/easystation/logging/LogManagementService.java`

- 获取日志文件列表
- 手动触发日志轮转
- 清理过期归档日志
- 获取日志统计信息

### 4. 创建日志管理 REST API
**文件**: `server/src/main/java/com/easystation/logging/LogResource.java`

- GET /api/v1/logs - 获取日志文件列表
- POST /api/v1/logs/rotate - 手动触发轮转
- DELETE /api/v1/logs/archive - 清理过期归档
- GET /api/v1/logs/stats - 获取日志统计

### 5. 创建定时清理任务
**文件**: `server/src/main/java/com/easystation/logging/LogCleanupScheduler.java`

- 每天凌晨清理过期归档
- 使用 Quarkus Scheduler

## 验收标准

- [ ] 日志文件超过 100MB 自动轮转
- [ ] 保留最近 10 个备份文件
- [ ] 旧日志自动压缩（.gz）
- [ ] 定时清理 30 天前的归档
- [ ] REST API 可查询日志状态
- [ ] 编译通过，测试通过

## 文件清单

### 新增文件
- `server/src/main/java/com/easystation/logging/LogRotationConfig.java`
- `server/src/main/java/com/easystation/logging/LogManagementService.java`
- `server/src/main/java/com/easystation/logging/LogResource.java`
- `server/src/main/java/com/easystation/logging/LogCleanupScheduler.java`
- `server/src/main/java/com/easystation/logging/dto/LogFileInfo.java`
- `server/src/main/java/com/easystation/logging/dto/LogStats.java`

### 修改文件
- `server/src/main/resources/application.properties` - 添加日志轮转配置

## 时间估算
- 配置日志轮转：30 分钟
- 创建配置类和服务：1 小时
- 创建 REST API：30 分钟
- 创建定时任务：30 分钟
- 测试验证：30 分钟
- **总计**: 约 3 小时

## 依赖关系
- 无外部依赖
- 使用 Quarkus 内置功能

## 风险
- 日志轮转可能影响性能（压缩操作）
- 需要确保归档目录有足够空间
