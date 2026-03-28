# Issue #140: 批量执行能力 - Step 2 后端服务实现

## 任务

创建批量操作服务类：

1. 创建 `server/src/main/java/com/easystation/agent/service/BatchOperationService.java`

   实现以下方法：
   - `createBatchCommand(List<UUID> hostIds, String command, UUID operatorId)` - 创建批量命令执行任务
   - `createBatchDeploy(List<UUID> agentIds, UUID operatorId)` - 创建批量部署任务
   - `createBatchUpgrade(List<UUID> agentIds, String version, UUID operatorId)` - 创建批量升级任务
   - `getBatchOperation(UUID id)` - 获取批量操作详情
   - `listBatchOperations(int page, int size)` - 分页查询批量操作历史
   - `updateItemStatus(UUID itemId, String status, String errorMessage)` - 更新子项状态
   - `checkBatchOperationCompletion(UUID operationId)` - 检查批量操作是否完成

2. 服务逻辑要求：
   - 创建批量操作时，同时创建所有子项（初始状态 PENDING）
   - 使用异步任务处理批量操作（使用 Quarkus @Asynchronous）
   - 更新子项状态时，自动计算并更新父操作的状态和计数
   - 所有子项完成后，更新父操作状态为 SUCCESS/PARTIAL_SUCCESS/FAILED

## 参考

- 参考现有的 Service 类结构（如 HostService, AgentInstanceService）
- 使用相同的包结构和命名规范
- 使用 Panache 进行数据库操作

## 输出

- 创建服务类文件
- 确保编译通过
