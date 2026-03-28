# 任务：命令模板管理前端页面 (#88)

## 背景
后端 API #71 命令模板管理已完成，需要开发配套前端页面。

## 需求

### 功能要求
1. **命令模板列表展示**
   - 表格展示：名称、描述、参数、创建时间、更新时间
   - 支持搜索（按名称/描述）
   - 支持分页

2. **新建/编辑命令模板**
   - 表单字段：
     - 名称（必填）
     - 描述（可选）
     - 命令内容（必填，支持变量语法 `${param}`）
     - 参数定义（JSON 格式，可选）
     - 分类/标签（可选）

3. **删除命令模板**
   - 确认对话框
   - 删除后刷新列表

4. **模板执行测试**
   - 点击"测试执行"按钮
   - 弹出表单输入参数值
   - 调用后端执行接口
   - 显示执行结果

### API 对接
后端接口（#71 已实现）：
- `GET /command-templates` - 获取列表
- `POST /command-templates` - 创建
- `GET /command-templates/{id}` - 获取详情
- `PUT /command-templates/{id}` - 更新
- `DELETE /command-templates/{id}` - 删除
- `POST /command-templates/{id}/execute` - 执行测试

### 技术栈
- React 18 + TypeScript
- Ant Design 组件库
- Axios 请求
- 项目现有代码规范

## 文件结构
参考现有页面（如 `AgentSourcePage.tsx`）：
- `frontend/src/pages/command/CommandTemplatePage.tsx` - 主页面
- `frontend/src/services/commandTemplate.ts` - API 服务
- `frontend/src/types/command.ts` - 类型定义（可选，如未定义则添加）

## 验收标准
- [ ] TypeScript 编译通过
- [ ] 前端构建成功（`npm run build`）
- [ ] Prettier 格式化通过
- [ ] 代码符合项目规范

## 执行方式
在 frontend 目录运行：
```bash
cd /home/esa-runner/es-agents/frontend
npm run build
```

## Git 身份
```bash
git config user.name "Y-Bot-N"
git config user.email "214893859@qq.com"
```

## 分支
创建分支：`feature/issue-88-command-template-ui`
