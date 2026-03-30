# Implementation Plan: 诊断报告生成 (#397)

## Overview

实现系统诊断报告自动生成功能，支持问题检测、报告生成和修复建议推荐。

**目标 Issue**: #397  
**优先级**: P2  
**里程碑**: M6 Phase 4  
**依赖**: #394 指标收集 ✅, #396 告警系统 ✅

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Diagnostic Report System                 │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ Diagnostic   │───▶│ Report       │───▶│ Export       │  │
│  │ Engine       │    │ Generator    │    │ Service      │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         │                    │                    │         │
│         ▼                    ▼                    ▼         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ Data Source  │    │ Report       │    │ PDF/HTML     │  │
│  │ - Metrics    │    │ Templates    │    │ Export       │  │
│  │ - Alerts     │    │              │    │              │  │
│  │ - Health     │    │              │    │              │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Tasks

### Phase 1: Core Domain Model (基础模型)

#### 1.1 Domain Entities

- [ ] `DiagnosticRule` - 诊断规则实体
  - ruleId, name, category, condition, severity
  - enabled, createdAt, updatedAt
- [ ] `DiagnosticReport` - 诊断报告实体
  - reportId, title, generatedAt, status
  - summary, findings, recommendations
- [ ] `DiagnosticFinding` - 诊断发现
  - findingId, ruleId, severity, description
  - impact, recommendation

#### 1.2 Enums

- [ ] `DiagnosticCategory` - 诊断类别（性能、安全、配置、资源）
- [ ] `ReportStatus` - 报告状态（生成中、完成、失败）
- [ ] `FindingSeverity` - 发现级别（信息、警告、严重、致命）

### Phase 2: Diagnostic Engine (诊断引擎)

#### 2.1 Rule Engine

- [ ] `DiagnosticRuleService` - 规则管理服务
  - CRUD 规则
  - 规则启用/禁用
  - 规则分类查询
- [ ] `DiagnosticEngine` - 诊断引擎核心
  - 规则执行器
  - 条件解析器
  - 结果聚合器

#### 2.2 Data Collectors

- [ ] `MetricDataCollector` - 指标数据收集器
  - 从 Prometheus/Micrometer 获取指标
  - 聚合历史数据
- [ ] `AlertDataCollector` - 告警数据收集器
  - 从告警系统获取数据
  - 统计告警趋势
- [ ] `HealthDataCollector` - 健康检查数据收集器
  - 系统健康状态
  - 服务可用性

#### 2.3 Built-in Rules

- [ ] CPU 使用率检查规则
- [ ] 内存使用率检查规则
- [ ] 磁盘空间检查规则
- [ ] 告警频率检查规则
- [ ] Agent 连接状态规则
- [ ] 任务失败率规则

### Phase 3: Report Generation (报告生成)

#### 3.1 Report Service

- [ ] `DiagnosticReportService` - 报告服务
  - 手动生成报告
  - 定时生成报告
  - 报告查询/删除
- [ ] `ReportTemplateService` - 模板服务
  - 报告模板管理
  - 模板渲染

#### 3.2 Export Service

- [ ] `ReportExportService` - 导出服务
  - PDF 导出（使用 iText 或 OpenPDF）
  - HTML 导出
  - 导出格式选择

#### 3.3 Scheduler

- [ ] `DiagnosticScheduler` - 定时任务调度
  - Cron 表达式配置
  - 定时生成报告
  - 手动触发接口

### Phase 4: REST API (接口层)

#### 4.1 Resources

- [ ] `DiagnosticRuleResource` - 规则管理 API
  - GET /api/diagnostic/rules
  - POST /api/diagnostic/rules
  - PUT /api/diagnostic/rules/{id}
  - DELETE /api/diagnostic/rules/{id}
- [ ] `DiagnosticReportResource` - 报告管理 API
  - POST /api/diagnostic/reports/generate
  - GET /api/diagnostic/reports
  - GET /api/diagnostic/reports/{id}
  - GET /api/diagnostic/reports/{id}/export?format=pdf|html
  - DELETE /api/diagnostic/reports/{id}

### Phase 5: Frontend (前端页面)

#### 5.1 Pages

- [ ] DiagnosticRules - 规则配置页面
- [ ] DiagnosticReports - 报告列表页面
- [ ] ReportDetail - 报告详情页面

#### 5.2 Components

- [ ] RuleEditor - 规则编辑器组件
- [ ] ReportViewer - 报告查看器组件
- [ ] FindingCard - 发现卡片组件

### Phase 6: Testing & Documentation (测试与文档)

- [ ] 单元测试覆盖
- [ ] 集成测试
- [ ] API 文档更新
- [ ] 用户指南

## Dependencies

### Maven Dependencies

```xml
<!-- PDF 生成 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- 模板引擎（如果需要复杂模板） -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-qute</artifactId>
</dependency>
```

## File Structure

```
server/src/main/java/com/easystation/diagnostic/
├── domain/
│   ├── DiagnosticRule.java
│   ├── DiagnosticReport.java
│   └── DiagnosticFinding.java
├── dto/
│   ├── DiagnosticRuleRecord.java
│   ├── DiagnosticReportRecord.java
│   └── DiagnosticFindingRecord.java
├── enums/
│   ├── DiagnosticCategory.java
│   ├── ReportStatus.java
│   └── FindingSeverity.java
├── service/
│   ├── DiagnosticRuleService.java
│   ├── DiagnosticEngine.java
│   ├── DiagnosticReportService.java
│   ├── ReportExportService.java
│   └── collectors/
│       ├── MetricDataCollector.java
│       ├── AlertDataCollector.java
│       └── HealthDataCollector.java
└── resource/
    ├── DiagnosticRuleResource.java
    └── DiagnosticReportResource.java

frontend/src/pages/diagnostic/
├── DiagnosticRules/
│   └── index.tsx
├── DiagnosticReports/
│   └── index.tsx
└── ReportDetail/
    └── index.tsx
```

## Acceptance Criteria

- [ ] 支持常见问题的自动诊断
- [ ] 报告包含问题描述和影响分析
- [ ] 提供可执行的修复建议
- [ ] 支持报告导出（PDF/HTML）
- [ ] 支持定时生成和手动触发
- [ ] 前端页面完整可用
- [ ] 单元测试覆盖率 > 80%

## Risk Assessment

| 风险             | 级别 | 缓解措施               |
| ---------------- | ---- | ---------------------- |
| PDF 生成复杂度高 | 中   | 使用简单模板，逐步增强 |
| 规则引擎性能     | 中   | 异步执行，结果缓存     |
| 数据收集延迟     | 低   | 设置合理超时           |

## Estimated Effort

- Phase 1 (Domain): 2h
- Phase 2 (Engine): 4h
- Phase 3 (Report): 3h
- Phase 4 (API): 2h
- Phase 5 (Frontend): 4h
- Phase 6 (Test & Doc): 2h

**Total**: ~17h

## Next Steps

1. 创建 feature 分支 `feature/diagnostic-report`
2. 按顺序实现各 Phase
3. 每个 Phase 完成后提交 PR
4. 全部完成后关闭 Issue #397
