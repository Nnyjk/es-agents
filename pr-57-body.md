## 描述
实现模板→实例→绑定可视化向导，帮助用户从模板创建实例并绑定到主机。

## 功能
- **步骤 1: 选择模板** - 从模板列表选择 Agent 模板
- **步骤 2: 配置实例** - 设置实例名称、环境、自定义配置
- **步骤 3: 绑定主机** - 选择目标主机（支持多选）
- **步骤 4: 预览配置** - 确认配置摘要
- **步骤 5: 创建结果** - 展示批量创建结果

## 组件
- `TemplateWizard`: 向导主页面
- `TemplateSelector`: 模板选择步骤
- `InstanceConfig`: 实例配置步骤
- `HostBinding`: 主机绑定步骤
- `ConfigPreview`: 配置预览步骤
- `CreationResult`: 创建结果展示

## 路由
- `/templates/new` - 创建向导入口

## 技术实现
- 5 步向导流程（Ant Design Steps）
- 状态管理（useState）
- 批量创建 API 调用
- 响应式布局

## 关联
- Closes #57
- 依赖 #60 (Environment API) ✅
- 依赖 #61 (Host API) ✅
- 依赖 #69 (Agent 模板 API) ✅
