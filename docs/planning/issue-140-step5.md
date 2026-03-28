# Issue #140: 批量执行能力 - Step 5 前端组件和页面

## 任务

创建前端批量操作组件和页面：

1. 创建 `frontend/src/components/batch/BatchOperationModal.tsx` + CSS

   组件功能：
   - 批量操作对话框（支持命令/部署/升级三种类型）
   - 目标选择（支持多选主机/Agent）
   - 操作参数输入（命令内容/版本等）
   - 实时进度展示
   - 结果汇总（成功/失败数量）
   - 失败项错误信息显示

2. 创建 `frontend/src/pages/batch/BatchOperationsPage.tsx` + CSS

   页面功能：
   - 批量操作历史列表（表格展示）
   - 状态筛选（全部/进行中/已完成/失败）
   - 操作类型筛选
   - 点击查看详情
   - 失败项重试功能

3. 创建 `frontend/src/pages/batch/BatchOperationDetailPage.tsx` + CSS

   详情页功能：
   - 批量操作基本信息
   - 子项列表（状态/目标/错误信息）
   - 实时刷新（进行中的操作）
   - 返回历史列表

4. 更新路由配置：
   - `/batch` - 批量操作历史页面
   - `/batch/:id` - 批量操作详情页

5. 更新菜单配置：
   - 在运维管理菜单下添加"批量操作"入口

## 参考

- 参考现有的页面和组件结构
- 使用 Ant Design 组件库
- 使用 CSS Modules 样式
- 遵循项目代码规范

## 输出

- 创建所有组件和页面文件
- 更新路由和菜单配置
- 确保 TypeScript 编译和 lint 通过
