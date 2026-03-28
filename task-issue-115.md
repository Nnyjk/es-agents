# 任务：配置管理前端页面 (#115)

## 背景
后端配置中心 API 已完成，需要实现前端管理页面。

## 需求
1. 配置项列表展示与搜索（ProTable）
2. 新建/编辑/删除配置表单（DrawerForm）
3. 配置版本历史查看与回滚
4. 不同环境配置差异对比
5. 对接后端配置中心 API (`/v1/configs`)

## 后端 API
- `GET /v1/configs` - 列表查询（支持 key, environmentId, group, active, limit, offset）
- `GET /v1/configs/{id}` - 获取详情
- `GET /v1/configs/key/{key}` - 按 key 查询
- `POST /v1/configs` - 创建配置
- `PUT /v1/configs/{id}` - 更新配置
- `DELETE /v1/configs/{id}` - 删除配置
- `POST /v1/configs/batch` - 批量更新
- `GET /v1/configs/{id}/history` - 配置历史
- `POST /v1/configs/history/{historyId}/rollback` - 回滚
- `GET /v1/configs/diff` - 环境对比
- `GET /v1/configs/groups` - 分组列表

## 需要创建的文件
1. `frontend/src/services/config.ts` - API 服务
2. `frontend/src/types/config.ts` - 类型定义（如已存在则更新）
3. `frontend/src/pages/config/ConfigPage.tsx` - 主页面组件
4. `frontend/src/App.tsx` - 添加路由 `/configs`

## 类型定义
- `ConfigItem` - 配置项（id, key, value, environmentId, group, description, active, createdAt, updatedAt）
- `ConfigHistory` - 配置历史（id, configId, oldValue, newValue, changeType, changedBy, changedAt）
- `ConfigDiff` - 配置差异对比结果

## 功能要求
- 列表展示：key, value, environment, group, active 状态
- 搜索：key, group, environment 筛选
- 操作：新建、编辑、删除、查看历史、回滚
- 环境对比：选择两个环境展示差异
- TypeScript 编译通过
- Prettier 格式化

## 验收标准
- [ ] 前端构建成功 (`npm run build`)
- [ ] TypeScript 无错误
- [ ] Prettier 格式化通过
- [ ] API 对接正确
- [ ] 路由配置正确

## 执行方式
使用 claude-code-executor skill 执行此任务。
