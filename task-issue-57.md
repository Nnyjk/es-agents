# Issue #57: 模板→实例→绑定可视化向导实现计划

## 目标
实现前端向导流程，帮助用户从模板创建实例并绑定到主机。

## 功能描述
- **步骤 1**: 选择 Agent 模板（从模板列表或市场）
- **步骤 2**: 配置实例参数（名称、环境、配置项）
- **步骤 3**: 选择目标主机（支持多选）
- **步骤 4**: 预览配置并确认创建
- **步骤 5**: 展示创建结果和绑定关系

## 技术栈
- React 18 + TypeScript
- Ant Design (Steps, Modal, Form, Select, Table)
- React Router
- 现有 API: `/api/agent-templates`, `/api/agent-instances`, `/api/hosts`

## 文件结构
```
frontend/src/pages/templates/
├── TemplateWizard.tsx         # 向导主页面
├── TemplateWizard.module.css
├── components/
│   ├── TemplateSelector.tsx   # 模板选择步骤
│   ├── InstanceConfig.tsx     # 实例配置步骤
│   ├── HostBinding.tsx        # 主机绑定步骤
│   ├── ConfigPreview.tsx      # 配置预览步骤
│   └── CreationResult.tsx     # 创建结果展示
└── types.ts                   # 类型定义
```

## 类型定义
```typescript
interface TemplateWizardState {
  currentStep: number;
  selectedTemplate?: AgentTemplate;
  instanceConfig: {
    name: string;
    environmentId?: string;
    config: Record<string, any>;
  };
  selectedHosts: string[];
  createdInstances: CreatedInstance[];
}

interface CreatedInstance {
  instanceId: string;
  name: string;
  hostId: string;
  status: 'success' | 'failed';
  error?: string;
}
```

## API 调用
1. `GET /api/agent-templates` - 获取模板列表
2. `GET /api/hosts` - 获取主机列表
3. `GET /api/environments` - 获取环境列表
4. `POST /api/agent-instances/batch` - 批量创建实例并绑定

## 实现步骤

### 1. 创建类型定义 (types.ts)
- TemplateWizardState
- CreatedInstance
- WizardStep 枚举

### 2. 创建向导组件 (TemplateWizard.tsx)
- 状态管理（使用 useState）
- 步骤导航（Ant Design Steps）
- 步骤切换逻辑
- API 调用封装

### 3. 创建各步骤组件
- **TemplateSelector**: 模板列表展示、搜索、选择
- **InstanceConfig**: 表单配置（名称、环境、自定义配置）
- **HostBinding**: 主机列表、多选、过滤
- **ConfigPreview**: 配置摘要展示
- **CreationResult**: 创建结果、成功/失败状态、跳转链接

### 4. 路由配置
- `/templates/new` - 创建向导入口
- 在模板列表页添加"创建实例"按钮

### 5. 样式优化
- 响应式布局
- 步骤指示器样式
- 结果页可视化

## 验收标准
- [ ] 步骤式向导完整可用
- [ ] 每步骤支持上一步/下一步导航
- [ ] 配置校验（必填项、格式）
- [ ] 批量创建支持
- [ ] 结果页展示成功/失败状态
- [ ] 响应式布局

## 依赖
- Issue #60: Environment 环境 CRUD API ✅
- Issue #61: Host 主机 CRUD API ✅
- Issue #69: Agent 模板管理 API ✅

## 里程碑
- M1 - MVP 版本（截止 2026-03-31）
