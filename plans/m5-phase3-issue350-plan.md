# M5 Phase 3 - Issue #350: 批量操作优化

## 目标
为 ESA 前端添加批量操作功能，提升用户操作效率。

## 技术方案

### 1. 批量选择 UI
- 列表项添加复选框
- 全选/取消全选功能
- 显示已选择数量

### 2. 批量操作 API
- 后端支持批量删除/更新接口
- 前端调用批量 API
- 处理部分成功/失败情况

### 3. 进度反馈
- 显示操作进度
- 成功/失败提示
- 错误处理

## 实现步骤

### Step 1: 后端批量 API
**文件**: `server/src/main/java/com/easystation/*/resource/*Resource.java`
- 添加 `@POST @Path("/batch-delete")` 接口
- 添加 `@POST @Path("/batch-update")` 接口
- 接收 ID 列表参数
- 返回操作结果统计

### Step 2: 批量选择组件
**文件**: `frontend/src/components/BatchSelection.tsx`
- 复选框组件
- 全选逻辑
- 选择状态管理

### Step 3: 批量操作栏
**文件**: `frontend/src/components/BatchActionBar.tsx`
- 显示已选择数量
- 批量删除按钮
- 批量更新按钮（如适用）
- 取消选择按钮

### Step 4: 列表集成
- 在相关文件列表中添加批量选择
- 在消息列表中添加批量选择
- 在其他适用列表中添加批量选择

### Step 5: 进度反馈
**文件**: `frontend/src/components/BatchProgress.tsx`
- 进度条显示
- 成功/失败统计
- 错误详情展示

### Step 6: 测试
- 测试批量删除功能
- 测试批量更新功能
- 测试错误处理
- 测试大量数据性能

## 验收标准
- [ ] 列表项可选择
- [ ] 全选/取消全选正常工作
- [ ] 批量删除功能正常
- [ ] 批量更新功能正常（如适用）
- [ ] 进度反馈清晰
- [ ] 错误处理完善
- [ ] 无控制台错误

## 相关文件
- `server/src/main/java/com/easystation/*/resource/*Resource.java`
- `frontend/src/components/BatchSelection.tsx`
- `frontend/src/components/BatchActionBar.tsx`
- `frontend/src/components/BatchProgress.tsx`

## 估计工时
- 后端开发：3-4 小时
- 前端开发：4-6 小时
- 测试：2-3 小时
