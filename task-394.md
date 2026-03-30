# Issue #394 - 指标收集与展示实现

## 目标
实现 ESA 系统的指标收集与可视化展示。

## 需求
- 系统指标收集（CPU、内存、磁盘、网络）
- 业务指标收集（Agent 数量、任务执行次数、成功率）
- Prometheus 集成
- 指标 API 端点（/metrics）
- 前端指标展示页面

## 实现任务
1. 添加 micrometer-registry-prometheus 依赖到 server/pom.xml
2. 创建 MetricsConfig.java 配置类
3. 创建 CustomMetrics.java 自定义指标
4. 创建 MetricsResource.java REST 端点
5. 创建前端 Metrics.tsx 页面
6. 更新路由和菜单配置

## 验收标准
- /metrics 端点返回 Prometheus 格式指标
- 前端页面展示核心指标
- 代码通过 pre-commit 检查
