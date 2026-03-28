# Issue #140: 批量执行能力 - Step 4 前端类型和服务

## 任务
创建前端类型定义和 API 服务：

1. 创建 `frontend/src/types/batch.ts`
   
   类型定义：
   ```typescript
   interface BatchOperation {
     id: string;
     operationType: 'BATCH_COMMAND' | 'BATCH_DEPLOY' | 'BATCH_UPGRADE';
     status: 'PENDING' | 'RUNNING' | 'PARTIAL_SUCCESS' | 'SUCCESS' | 'FAILED';
     operatorId: string;
     createdAt: string;
     completedAt?: string;
     totalItems: number;
     successCount: number;
     failedCount: number;
   }

   interface BatchOperationItem {
     id: string;
     batchOperationId: string;
     targetId: string;
     targetName?: string;
     targetType: 'HOST' | 'AGENT';
     status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED';
     errorMessage?: string;
     startedAt?: string;
     completedAt?: string;
   }

   interface BatchCommandRequest {
     hostIds: string[];
     command: string;
   }

   interface BatchDeployRequest {
     agentIds: string[];
   }

   interface BatchUpgradeRequest {
     agentIds: string[];
     version: string;
   }
   ```

2. 创建 `frontend/src/services/batch.ts`
   
   API 服务函数：
   - `batchExecuteCommand(request: BatchCommandRequest): Promise<BatchOperation>`
   - `batchDeploy(request: BatchDeployRequest): Promise<BatchOperation>`
   - `batchUpgrade(request: BatchUpgradeRequest): Promise<BatchOperation>`
   - `getBatchOperation(id: string): Promise<BatchOperation>`
   - `getBatchOperationItems(id: string): Promise<BatchOperationItem[]>`
   - `listBatchOperations(page: number, size: number): Promise<{ items: BatchOperation[], total: number }>`

## 参考
- 参考现有的 types 和 services 文件结构
- 使用相同的 API 调用模式（fetch 或 axios）
- 添加适当的错误处理

## 输出
- 创建类型和服务文件
- 确保 TypeScript 编译通过
