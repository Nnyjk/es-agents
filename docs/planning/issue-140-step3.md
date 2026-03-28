# Issue #140: 批量执行能力 - Step 3 后端 API 实现

## 任务

创建批量操作 REST API：

1. 创建 DTO 类：
   - `server/src/main/java/com/easystation/agent/dto/BatchCommandRequest.java` - 批量命令请求
   - `server/src/main/java/com/easystation/agent/dto/BatchDeployRequest.java` - 批量部署请求
   - `server/src/main/java/com/easystation/agent/dto/BatchUpgradeRequest.java` - 批量升级请求
   - `server/src/main/java/com/easystation/agent/dto/BatchOperationResponse.java` - 批量操作响应
   - `server/src/main/java/com/easystation/agent/dto/BatchOperationItemResponse.java` - 批量操作项响应

2. 创建 `server/src/main/java/com/easystation/agent/resource/BatchOperationResource.java`

   API 端点：
   - `POST /api/v1/batch-operations/commands` - 批量执行命令
   - `POST /api/v1/batch-operations/deploy` - 批量部署
   - `POST /api/v1/batch-operations/upgrade` - 批量升级
   - `GET /api/v1/batch-operations/{id}` - 获取操作详情
   - `GET /api/v1/batch-operations/{id}/items` - 获取子项列表
   - `GET /api/v1/batch-operations` - 分页查询历史

3. 添加权限控制：
   - 所有端点需要 `batch:operate` 权限

## 参考

- 参考现有的 Resource 类结构（如 HostResource, AgentInstanceResource）
- 使用相同的包结构和命名规范
- 添加 OpenAPI 注解

## 输出

- 创建所有 DTO 和资源类文件
- 确保编译通过
