# Issue #33: 目标驱动 Goal Hub 页面实现计划

## 目标
实现目标驱动的首页（Goal Hub），以"我要做什么"为入口展示目标卡片列表。

## 功能描述
- 首页展示目标卡片列表（部署 Agent、执行命令、查看状态、资源配置等）
- 每个卡片包含：图标、标题、描述、操作按钮
- 点击卡片跳转至对应功能页面/向导
- 响应式布局适配不同屏幕尺寸

## 技术栈
- React 18 + TypeScript
- Ant Design (Card, Row, Col, Typography)
- React Router
- CSS Modules / Flexbox 布局

## 文件结构
```
frontend/src/pages/goal-hub/
├── GoalHub.tsx              # 主页面组件
├── GoalHub.module.css       # 样式文件
├── components/
│   └── GoalCard.tsx         # 目标卡片组件
├── index.ts                 # 导出
└── types.ts                 # 类型定义
```

## 类型定义
```typescript
interface GoalCard {
  id: string;
  title: string;
  description: string;
  icon: string; // 图标名称或 SVG
  actionText: string;
  route: string;
  color?: string;
}
```

## 目标卡片列表
1. **部署 Agent** - 从模板创建并部署 Agent 实例 → `/templates/new`
2. **查看 Agent 状态** - 查看 Agent 实例列表和状态 → `/agents`
3. **执行命令** - 向 Agent 发送命令 → `/agents` (带操作)
4. **管理主机** - 管理目标主机资源 → `/hosts`
5. **管理环境** - 管理环境配置 → `/environments`
6. **查看部署历史** - 查看部署记录和状态 → `/deployments`

## 实现步骤

### 1. 创建类型定义 (types.ts)
- GoalCard 接口定义

### 2. 创建 GoalCard 组件 (components/GoalCard.tsx)
- Ant Design Card 封装
- 图标、标题、描述、操作按钮
- hover 效果和点击跳转

### 3. 创建 GoalHub 主页面 (GoalHub.tsx)
- 目标卡片数据数组
- 响应式网格布局（Row/Col）
- 页面标题和描述

### 4. 创建样式文件 (GoalHub.module.css)
- 卡片样式
- 网格间距
- 响应式断点

### 5. 路由配置
- 在 App.tsx 中添加 `/` 或 `/goals` 路由
- 设置为默认首页

### 6. 导航菜单更新
- 在侧边栏添加"目标中心"或"首页"入口

## 验收标准
- [ ] 首页显示至少 6 个目标卡片
- [ ] 卡片可点击跳转至对应功能
- [ ] 响应式布局（桌面/平板/手机）
- [ ] 卡片样式美观，符合 Ant Design 规范
- [ ] TypeScript 编译通过

## 依赖
- 无后端依赖（纯前端页面）
- 依赖现有路由和页面（模板向导、Agent 列表等）

## 里程碑
- M1 - MVP 版本（截止 2026-03-31）
