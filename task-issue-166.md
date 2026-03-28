# 任务：Prometheus 监控与 Grafana 大盘集成前端页面 (#166)

## 背景
M3 里程碑高优先级任务，配合后端 #133，实现监控大盘可视化。

## 需求概述
1. **监控概览页面** - 核心指标卡片、资源使用率趋势图、业务指标展示
2. **Grafana 大盘集成** - 嵌入 Grafana 面板、自定义大盘管理
3. **指标查询功能** - PromQL 编辑器、可视化展示
4. **告警关联** - 阈值配置、告警联动展示

## 需要创建的文件
1. `frontend/src/pages/monitoring/MonitoringPage.tsx` - 监控概览主页面
2. `frontend/src/pages/monitoring/GrafanaDashboardPage.tsx` - Grafana 大盘管理页面
3. `frontend/src/services/monitoring.ts` - 监控 API 服务
4. `frontend/src/types/monitoring.ts` - 监控类型定义
5. `frontend/src/components/monitoring/MetricCard.tsx` - 指标卡片组件
6. `frontend/src/components/monitoring/MetricChart.tsx` - 指标图表组件
7. `frontend/src/components/monitoring/PromQLEditor.tsx` - PromQL 编辑器组件
8. `frontend/src/components/monitoring/GrafanaPanel.tsx` - Grafana 面板组件

## 路由配置
- `/monitoring` - 监控概览
- `/monitoring/grafana` - Grafana 大盘管理
- `/monitoring/query` - 指标查询

## API 端点 (后端 #133)
- GET /v1/metrics/summary - 获取监控概览数据
- GET /v1/metrics/timeseries - 获取时序指标数据
- GET /v1/metrics/promql - PromQL 查询
- GET /v1/grafana/dashboards - 获取大盘列表
- POST /v1/grafana/dashboards - 创建大盘
- PUT /v1/grafana/dashboards/:id - 更新大盘
- DELETE /v1/grafana/dashboards/:id - 删除大盘

## 验收标准
- [ ] 页面风格与现有前端一致 (Ant Design + CSS Modules)
- [ ] Grafana 面板嵌入正常，无跨域问题
- [ ] 指标查询响应快速，可视化展示清晰
- [ ] 支持自定义监控大盘保存和分享
- [ ] 操作成功/失败有明确提示

## 技术栈
- React 18+
- TypeScript
- Ant Design
- Recharts (图表库)
- CSS Modules

## 注意事项
- 需要处理 Grafana 嵌入的跨域问题
- 时序数据需要支持时间范围选择
- PromQL 编辑器需要语法高亮
- 图表需要支持响应式布局
