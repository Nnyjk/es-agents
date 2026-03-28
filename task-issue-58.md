# Issue #58: Agent 资源拉取与打包部署执行

## 目标
实现 Agent 端资源来源的拉取、校验，以及打包/部署动作的具体执行。

## 实现内容

### 1. 资源拉取器 (`agent/internal/resource/fetcher.go`)
支持多种资源来源：
- 本地路径（Local）
- Git 仓库（Git）
- HTTP/HTTPS 下载（HTTP）
- Docker 仓库（Docker）
- 阿里云 OSS（AliyunOSS）

### 2. 资源校验器 (`agent/internal/resource/validator.go`)
- 版本校验
- 完整性校验（checksum）
- 签名校验（可选）

### 3. 打包执行器 (`agent/internal/resource/packer.go`)
- 构建可部署单元
- 生成部署包

### 4. 部署执行器 (`agent/internal/resource/deployer.go`)
- 投放到目标主机
- 启动服务
- 自检准备（健康检查）

### 5. Agent 消息处理 (`agent/internal/app/agent.go`)
添加新的消息类型：
- `FETCH_RESOURCE` - 拉取资源
- `BUILD_PACKAGE` - 打包
- `DEPLOY` - 部署
- `HEALTH_CHECK` - 健康检查

## 验收标准
- [ ] 多种资源来源拉取支持
- [ ] 资源拉取前校验
- [ ] 打包/部署动作正确执行
- [ ] 健康检查通过
- [ ] Go 代码通过 `go build ./...` 编译验证

## 关联
- Closes #58
