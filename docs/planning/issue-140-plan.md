# Issue #140: 操作批量执行能力 - 实现计划

## 需求分析

实现批量操作能力，提升运维效率：
1. 批量选择主机/Agent 执行命令
2. 批量部署 Agent 到多台主机
3. 批量升级 Agent 版本
4. 批量操作结果汇总展示，失败单独处理

## 技术方案

### 后端实现

#### 1. 实体设计 (`BatchOperationRecord.java`)

```java
@Entity
@Table(name = "batch_operations")
public class BatchOperationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String operationType; // BATCH_COMMAND, BATCH_DEPLOY, BATCH_UPGRADE
    private String status; // PENDING, RUNNING, PARTIAL_SUCCESS, SUCCESS, FAILED
    private UUID operatorId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "batchOperation")
    private List<BatchOperationItemRecord> items;
}

@Entity
@Table(name = "batch_operation_items")
public class BatchOperationItemRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "batch_operation_id")
    private BatchOperationRecord batchOperation;
    
    private UUID targetId; // hostId or agentId
    private String targetType; // HOST, AGENT
    private String status; // PENDING, RUNNING, SUCCESS, FAILED
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
```

#### 2. 服务层 (`BatchOperationService.java`)

- `createBatchCommand(List<UUID> hostIds, CommandDTO)` - 批量执行命令
- `createBatchDeploy(List<UUID> agentIds, DeployDTO)` - 批量部署
- `createBatchUpgrade(List<UUID> agentIds, String version)` - 批量升级
- `getBatchOperationStatus(UUID id)` - 获取批量操作状态
- `updateItemStatus(UUID itemId, String status, String error)` - 更新子项状态

#### 3. 资源层 (`BatchOperationResource.java`)

```java
@Path("/api/v1/batch-operations")
public class BatchOperationResource {
    @POST
    @Path("/commands")
    public Response batchExecuteCommand(@Valid BatchCommandRequest request);
    
    @POST
    @Path("/deploy")
    public Response batchDeploy(@Valid BatchDeployRequest request);
    
    @POST
    @Path("/upgrade")
    public Response batchUpgrade(@Valid BatchUpgradeRequest request);
    
    @GET
    @Path("/{id}")
    public Response getOperationStatus(@PathParam("id") UUID id);
    
    @GET
    public Response listOperations(@QueryParam("page") int page, @QueryParam("size") int size);
}
```

### 前端实现

#### 1. 类型定义 (`types/batch.ts`)

```typescript
interface BatchOperation {
  id: string;
  operationType: 'BATCH_COMMAND' | 'BATCH_DEPLOY' | 'BATCH_UPGRADE';
  status: 'PENDING' | 'RUNNING' | 'PARTIAL_SUCCESS' | 'SUCCESS' | 'FAILED';
  createdAt: string;
  completedAt?: string;
  totalItems: number;
  successCount: number;
  failedCount: number;
}

interface BatchOperationItem {
  id: string;
  targetId: string;
  targetName: string;
  targetType: 'HOST' | 'AGENT';
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED';
  errorMessage?: string;
}
```

#### 2. 服务层 (`services/batch.ts`)

- `batchExecuteCommand(hostIds, command)` 
- `batchDeploy(agentIds, deployConfig)`
- `batchUpgrade(agentIds, version)`
- `getBatchOperationStatus(id)`
- `getBatchOperationItems(id)`

#### 3. 组件 (`components/batch/BatchOperationModal.tsx`)

- 批量操作对话框
- 目标选择（支持多选）
- 进度展示
- 结果汇总

#### 4. 页面 (`pages/batch/BatchOperationsPage.tsx`)

- 批量操作历史列表
- 操作详情查看
- 失败重试功能

## 实现步骤

### Step 1: 后端实体创建
- [ ] 创建 `BatchOperationRecord.java`
- [ ] 创建 `BatchOperationItemRecord.java`
- [ ] 添加数据库迁移脚本

### Step 2: 后端服务实现
- [ ] 创建 `BatchOperationService.java`
- [ ] 实现批量命令执行逻辑
- [ ] 实现批量部署逻辑
- [ ] 实现批量升级逻辑
- [ ] 添加异步任务处理

### Step 3: 后端 API 实现
- [ ] 创建 `BatchOperationResource.java`
- [ ] 创建请求/响应 DTO
- [ ] 添加权限控制

### Step 4: 前端类型和服务
- [ ] 创建 `types/batch.ts`
- [ ] 创建 `services/batch.ts`

### Step 5: 前端组件和页面
- [ ] 创建 `BatchOperationModal.tsx`
- [ ] 创建 `BatchOperationsPage.tsx`
- [ ] 集成到路由

### Step 6: 测试
- [ ] 后端单元测试
- [ ] 前端集成测试
- [ ] 手动测试批量操作

## 验收标准

- [ ] 能够批量选择多台主机执行命令
- [ ] 能够批量部署 Agent 到多个主机
- [ ] 能够批量升级 Agent 版本
- [ ] 批量操作结果汇总展示（成功/失败数量）
- [ ] 失败项单独显示错误信息
- [ ] 支持失败项重试
- [ ] 批量操作历史可查询

## 风险与依赖

- 依赖现有的 Host 和 Agent API
- 需要异步任务处理支持
- 批量操作可能需要较长时间，需要进度跟踪
