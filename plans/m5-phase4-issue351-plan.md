# M5 Phase 4 - Issue #351: 健康检查增强

## 目标

增强健康检查功能，提升系统可观测性。

## 功能范围

1. **深度健康检查**
   - 数据库连接检查
   - Redis 连接检查
   - Agent 实例状态检查

2. **健康检查 API**
   - GET `/api/v1/health` - 总体健康状态
   - GET `/api/v1/health/live` - 存活检查
   - GET `/api/v1/health/ready` - 就绪检查

3. **健康状态监控**
   - 健康状态聚合
   - 异常自动告警

## 实现步骤

### Step 1: 添加依赖
- [x] 添加 `quarkus-smallrye-health` 到 `server/pom.xml`

### Step 2: 创建 DTOs
- [ ] `HealthStatus.java` - 健康状态枚举
- [ ] `HealthCheckResult.java` - 单项检查结果
- [ ] `HealthSummary.java` - 健康汇总信息

### Step 3: 实现健康检查
- [ ] `DatabaseHealthCheck.java` - 数据库健康检查
- [ ] `RedisHealthCheck.java` - Redis 健康检查
- [ ] `AgentHealthCheck.java` - Agent 实例健康检查

### Step 4: 创建 API 端点
- [ ] `HealthResource.java` - 健康检查 REST API

### Step 5: 前端健康状态页面
- [ ] 健康状态展示组件
- [ ] 健康状态详情页面

## 验收标准

- [ ] 健康检查响应时间 < 100ms
- [ ] 支持自定义检查项
- [ ] 异常自动告警
- [ ] 健康状态可视化

## 依赖

- #341 Redis 缓存集成
