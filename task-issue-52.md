# Issue #52: 部署流程前端串联与状态展示实现计划

## 目标
完成部署流程前端的完整串联，实现从创建部署到实时进度展示的完整流程。

## 技术栈
- React 18 + TypeScript
- Ant Design 组件库 (Steps, Progress, Modal)
- WebSocket 实时通信
- React Router 路由

## 文件结构
```
frontend/src/pages/deployment/
├── DeploymentWizard.tsx      # 部署向导主页面
├── DeploymentWizard.module.css
├── components/
│   ├── DeploymentSteps.tsx   # 步骤导航
│   ├── DeploymentProgress.tsx # 实时进度展示
│   └── DeploymentResult.tsx  # 结果展示
└── types.ts                  # 类型定义
```

## 实现步骤

### 1. 类型定义 (types.ts)
- DeploymentStep: 部署步骤枚举
- DeploymentProgress: 进度数据结构
- DeploymentStatus: 状态枚举 (PENDING, RUNNING, SUCCESS, FAILED)

### 2. 部署向导 (DeploymentWizard.tsx)
- 步骤 1: 选择 Agent/环境
- 步骤 2: 选择部署包/版本
- 步骤 3: 配置参数
- 步骤 4: 确认并提交
- 步骤 5: 实时进度展示
- 步骤 6: 结果展示

### 3. WebSocket 集成
- 连接 `/ws/deployment/{deploymentId}`
- 监听消息类型:
  - `DEPLOYMENT_PROGRESS`: 进度更新
  - `DEPLOYMENT_STATUS`: 状态变更
  - `DEPLOYMENT_ERROR`: 错误信息
- 自动重连机制

### 4. 进度展示组件
- 使用 Progress 组件显示百分比
- 使用 Steps 组件显示当前阶段
- 实时日志输出区域
- 错误提示与重试按钮

### 5. 路由配置
- `/deployments/new` - 创建新部署
- `/deployments/:id` - 查看部署详情/进度

## 验收标准
- [ ] 完整部署向导流程
- [ ] 实时进度展示 (WebSocket)
- [ ] 结果页展示
- [ ] 错误提示与重试
- [ ] 响应式布局

## 依赖
- Issue #51: 部署流程 WebSocket 实时推送 (已完成)
- Issue #42: Agent 任务结果回调 API (已完成)
