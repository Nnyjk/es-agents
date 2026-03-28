# Issue #44: Agent 实例详情页前端实现计划

## 目标
实现 Agent 实例详情页面，展示状态、日志、部署历史等信息。

## 技术栈
- React 18 + TypeScript
- Ant Design 组件库
- WebSocket 实时通信
- React Router 路由

## 文件结构
```
frontend/src/pages/agent/detail/
├── AgentDetail.tsx          # 主页面组件
├── AgentDetail.module.css   # 样式
├── index.ts                 # 导出文件
├── types.ts                 # 类型定义
├── types.css.d.ts           # CSS 模块类型声明
└── components/
    ├── AgentStatusCard.tsx  # 状态卡片
    ├── AgentLogViewer.tsx   # 日志查看器
    ├── AgentHistory.tsx     # 部署历史
    └── AgentActions.tsx     # 快捷操作
```

## 实现步骤

### 1. 类型定义 (types.ts)
- AgentDetailData: Agent 详情数据结构
- AgentStatus: 状态枚举 (RUNNING, STOPPED, DEPLOYING, FAILED)
- AgentLog: 日志条目结构
- DeploymentHistory: 部署历史记录

### 2. API 服务 (services/agent.ts)
- getAgentDetail(id: string): Promise<AgentDetailData>
- getAgentLogs(id: string, options?: { limit?: number }): Promise<AgentLog[]>
- getAgentHistory(id: string): Promise<DeploymentHistory[]>
- executeAgentAction(id: string, action: string): Promise<void>

### 3. 组件实现

#### AgentStatusCard
- 展示 Agent 基本信息（名称、版本、状态）
- 状态机可视化（使用 Steps 或 Timeline 组件）
- 实时状态更新（WebSocket）

#### AgentLogViewer
- 实时日志流展示
- 日志级别过滤（INFO/WARN/ERROR）
- 自动滚动到底部
- 暂停/继续功能

#### AgentHistory
- 部署历史列表
- 时间线展示
- 点击查看详情

#### AgentActions
- 部署按钮
- 重启按钮
- 删除按钮（带确认）
- 根据状态禁用/启用操作

### 4. 路由配置
- 在 App.tsx 添加路由：/agents/:id

### 5. WebSocket 集成
- 连接 /ws/agent/{agentId}
- 监听状态变更事件
- 自动重连机制

## 验收标准
- [x] 页面展示 Agent 基本信息
- [x] 实时状态更新（WebSocket）
- [x] 日志查看功能正常
- [x] 部署历史展示
- [x] 快捷操作可用
- [x] 响应式布局

## 依赖
- Issue #42: Agent 任务结果回调 API (已完成)
- Issue #51: 部署流程 WebSocket 实时推送 (已完成)
