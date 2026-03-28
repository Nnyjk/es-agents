## 描述
实现 Agent 实例详情页面，展示状态、日志、部署历史等信息。

## 功能
- **Agent 基本信息展示**: 名称、版本、状态、主机信息
- **实时状态可视化**: 状态机流转，WebSocket 实时更新
- **日志查看器**: 实时日志流、级别过滤、自动滚动
- **部署历史**: 时间线展示部署记录
- **快捷操作**: 部署、重启、删除（带状态感知）

## 组件
- `AgentDetail`: 主页面组件
- `AgentStatusCard`: 状态卡片
- `AgentLogViewer`: 日志查看器
- `AgentHistory`: 部署历史
- `AgentActions`: 快捷操作

## 路由
- `/agents/:id` - Agent 详情页

## 技术实现
- WebSocket 连接 `/ws/agent/{agentId}` 接收实时状态更新
- 支持日志级别过滤（DEBUG/INFO/WARN/ERROR）
- 响应式布局，适配不同屏幕尺寸

## 关联
- Closes #44
- 依赖 #42 (Agent 任务结果回调 API) ✅
- 依赖 #51 (部署流程 WebSocket 实时推送) ✅
