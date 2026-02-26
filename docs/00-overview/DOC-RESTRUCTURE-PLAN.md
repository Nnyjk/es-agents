# 文档收敛方案（Issue #3）

## 目标
- 解决 `docs/01-requirements/` 下需求文档语义重叠问题。
- 建立单一事实来源（SSOT）：`docs/01-requirements/easy-station-requirements.md`。
- 将重复文档收敛为“引用型文档”，仅保留用途说明与跳转关系。

## 范围
- 主文档（保留并完善）：`docs/01-requirements/easy-station-requirements.md`
- 收敛对象（合并/简化）：
  - `docs/01-requirements/01-product-definition.md`
  - `docs/01-requirements/02-functional-specs.md`
  - `docs/01-requirements/03-user-interaction.md`
- 索引更新：`docs/00-overview/DOCS-INDEX.md`

## 重叠分析结论

### 重复定义的核心概念
- 系统边界与子系统职责：`Server / Agent / Frontend / Proxy`
- 基础实体：`Environment / Host`
- Agent 相关实体：`AgentTemplate / AgentInstance / Binding`
- 动作与状态：`Deployment / Command / Status / Log / Resource Source`
- 角色与权限：`Admin / Ops / Dev / Viewer`

以上概念已在主文档中完整出现，且范围更广（含模板-实例-绑定、状态机、异常场景、验收用例）。

### 重复描述的功能规格
- 环境/主机管理、Host Agent 纳管流程
- 主机下新增/移除 Agent 实例
- 命令执行与终端交互
- Agent 模板与资源来源配置
- 打包/部署生命周期流程
- 状态/日志/审计要求
- 用户交互导航、关键任务流程（部署 Agent、执行命令、维护模板）

以上内容在 `02-functional-specs.md` 和 `03-user-interaction.md` 中与主文档高度重合，差异主要是表述粒度而非事实差异。

## 收敛策略

### 1. 主文档作为唯一事实来源（SSOT）
- 保留 `easy-station-requirements.md` 作为需求定义主文档。
- 该文档继续承载：
  - 概念定义
  - 角色与权限
  - 目标与任务地图
  - 关键业务流程
  - 模块职责
  - 状态模型
  - 功能需求清单
  - 异常与边界
  - 验收标准

### 2. 其他文档改为引用型文档
- `01-product-definition.md`：保留“文档定位 + 覆盖范围说明 + 主文档引用”。
- `02-functional-specs.md`：保留“功能规格入口 + 主文档对应章节映射”。
- `03-user-interaction.md`：保留“交互流程入口 + 主文档对应章节映射”。
- 不再重复定义任何核心概念或功能细节。

### 3. 索引调整
- 在 `DOCS-INDEX.md` 中明确：
  - `easy-station-requirements.md` 为需求唯一事实来源
  - `01/02/03` 为历史拆分入口/引用页（避免重复维护）

## 执行步骤
1. 新增本方案文档（当前文件）。
2. 简化 `01-product-definition.md` 为引用型文档。
3. 简化 `02-functional-specs.md` 为引用型文档。
4. 简化 `03-user-interaction.md` 为引用型文档。
5. 更新 `DOCS-INDEX.md` 的阅读顺序与文档域说明。
6. 检查内部链接与路径是否有效。
7. 提交变更并记录完成事件。

## 验收对照（Issue #3）
- 核心概念仅在主文档定义：通过将 `01/02/03` 改为引用型文档实现。
- 功能规格无重复描述：通过删除重复功能条目并改为映射引用实现。
- 文档索引反映新层级：更新 `DOCS-INDEX.md`。
- 内部链接正确：统一使用相对路径指向主文档与相关文件。
