# 本地开发环境配置指南

本文档说明如何在本地搭建 es-agents 项目的开发环境，包括前端、服务端和 HostAgent 的启动、联调与验证流程。

## 系统要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | 20.x | 前端开发 |
| Java | 21.x | 服务端开发 (Temurin 推荐) |
| Go | 1.23+ | Agent 开发 |
| Maven | 3.8+ | 服务端构建 |
| Git | 2.x+ | 版本控制 |

## 环境安装

### 1. Node.js (前端)

```bash
# 使用 nvm 安装 (推荐)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 20
nvm use 20

# 验证
node --version  # v20.x.x
npm --version   # 10.x.x
```

### 2. Java (服务端)

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# 或使用 SDKMAN (推荐)
curl -s "https://get.sdkman.io" | bash
sdk install java 21.0.2-tem

# 验证
java --version  # 21.x.x
javac --version # 21.x.x
```

### 3. Go (Agent)

```bash
# 下载并安装
wget https://go.dev/dl/go1.23.0.linux-amd64.tar.gz
sudo tar -C /usr/local -xzf go1.23.0.linux-amd64.tar.gz
export PATH=$PATH:/usr/local/go/bin

# 或使用权限管理工具
go install golang.org/dl/go1.23.0@latest
go1.23.0 download

# 验证
go version  # go version go1.23.x
```

### 4. Maven (服务端)

```bash
# Ubuntu/Debian
sudo apt install maven

# 或使用 SDKMAN
sdk install maven

# 验证
mvn --version  # Apache Maven 3.8+
```

## 项目克隆

```bash
git clone https://github.com/easy-station/es-agents.git
cd es-agents
```

## 快速开始

推荐按下面顺序启动本地联调环境：

```bash
# 1. 启动 PostgreSQL（本机已有则跳过）
docker run --name es-agents-postgres \
  -e POSTGRES_DB=easy_station \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16

# 2. 启动服务端
./scripts/dev-server.sh

# 3. 启动前端（新终端）
./scripts/dev-frontend.sh

# 4. 生成并启动 host-agent（新终端）
./scripts/dev-host-agent-config.sh
./scripts/dev-host-agent.sh
```

默认端口：

- Frontend: `http://127.0.0.1:5173`
- Server API: `http://127.0.0.1:8080`
- HostAgent WebSocket 监听: `http://127.0.0.1:9090/ws`

如需覆盖本地默认配置，可在启动前设置环境变量：

```bash
export DB_URL=jdbc:postgresql://127.0.0.1:5432/easy_station
export DB_USER=postgres
export DB_PASSWORD=postgres
export HOST_AGENT_ID=local-host
export HOST_AGENT_SECRET=local-secret
export HOST_AGENT_PORT=9090
```

## 前端开发环境

### 安装依赖

```bash
cd frontend
npm ci --legacy-peer-deps
```

### 开发服务器

```bash
npm run dev
```

访问 `http://localhost:5173` (Vite 默认端口)

### 构建检查

```bash
# 构建
npm run build

# 测试
npm test

# 类型检查
npx tsc --noEmit
```

### 代码格式化

```bash
npm run lint
npm run format
```

## 服务端开发环境

### 构建项目

```bash
cd server
mvn -B -DskipTests package --file pom.xml
```

### 运行测试

```bash
mvn -B test --file pom.xml
```

### 本地运行

```bash
# 推荐：使用调试脚本（默认指向本机 PostgreSQL）
./scripts/dev-server.sh

# 等价手动方式
export DB_URL=jdbc:postgresql://127.0.0.1:5432/easy_station
export DB_USER=postgres
export DB_PASSWORD=postgres
cd server
mvn quarkus:dev --file pom.xml
```

## Agent 开发环境

### 构建

```bash
cd agent
CGO_ENABLED=0 go build -v ./...
```

### 运行测试

```bash
go test -v ./...
```

### 代码检查

```bash
go vet ./...
```

### 本地运行

```bash
# 生成调试配置
./scripts/dev-host-agent-config.sh

# 启动 host-agent
./scripts/dev-host-agent.sh

# 等价手动方式
cd agent
go run ./cmd/host-agent --config ./.dev/config.yaml
```

`host-agent` 按纯 Go 静态构建维护，使用 `CGO_ENABLED=0` 和构建标签 `netgo osusergo` 确保无 GLIBC 依赖。CI 包含 `verify-linux-compat.sh` 脚本验证二进制文件为静态链接，本地开发可选运行该脚本验证构建结果。

## 本地调试脚本

仓库根目录提供以下脚本：

| 脚本 | 作用 |
|------|------|
| `scripts/dev-frontend.sh` | 安装前端依赖（如缺失）并启动 Vite 开发服务器 |
| `scripts/dev-server.sh` | 设置本地数据库默认值并启动 Quarkus dev mode |
| `scripts/dev-host-agent-config.sh` | 生成 `agent/.dev/config.yaml` |
| `scripts/dev-host-agent.sh` | 使用调试配置启动 HostAgent |

`scripts/dev-host-agent-config.sh` 支持可选输出路径参数：

```bash
./scripts/dev-host-agent-config.sh /tmp/host-agent-debug.yaml
./scripts/dev-host-agent.sh /tmp/host-agent-debug.yaml
```

## 完整本地验证流程

在提交代码前，建议在本地执行完整检查：

```bash
#!/bin/bash
# 在项目根目录执行

echo "=== Frontend Checks ==="
cd frontend
npm ci --legacy-peer-deps
npm run build
npm test
npx tsc --noEmit
cd ..

echo "=== Server Checks ==="
mvn -B test --file server/pom.xml
mvn -B -DskipTests package --file server/pom.xml

echo "=== Agent Checks ==="
cd agent
go test -v ./...
go vet ./...
CGO_ENABLED=0 go build -v ./...
cd ..

echo "=== Repository Lint ==="
npx --yes prettier@3.5.3 --check ".github/**/*.{yml,yaml,md}" "docs/**/*.md" "frontend/src/**/*.{ts,tsx}"

# 检查未解决的合并标记
if grep -R -nE --exclude-dir=.git "^(<<<<<<<|=======|>>>>>>>)" .; then
    echo "❌ Unresolved merge markers detected."
    exit 1
fi

echo "✅ All checks passed!"
```

## 常见问题

### Frontend

**Q: npm install 失败**
```bash
# 清理缓存
npm cache clean --force
rm -rf node_modules package-lock.json
npm ci --legacy-peer-deps
```

**Q: TypeScript 类型错误**
```bash
# 检查 TypeScript 配置
npx tsc --showConfig
```

### Server

**Q: Maven 构建失败**
```bash
# 清理并重新构建
mvn clean
mvn -B -DskipTests package --file server/pom.xml
```

**Q: Java 版本不匹配**
```bash
# 检查当前 Java 版本
java --version
# 使用 SDKMAN 切换
sdk use java 21.0.2-tem
```

### Agent

**Q: Go 模块下载失败**
```bash
# 清理模块缓存
go clean -modcache
go mod download
```

**Q: HostAgent 启动失败或配置不生效**
```bash
# 重新生成本地配置
./scripts/dev-host-agent-config.sh

# 查看实际使用的配置
cat agent/.dev/config.yaml

# 使用显式配置启动
./scripts/dev-host-agent.sh "$(pwd)/agent/.dev/config.yaml"
```

## IDE 配置建议

### VS Code 推荐插件

- **Frontend**: ESLint, Prettier, TypeScript
- **Server**: Language Support for Java, Maven for Java
- **Agent**: Go, Go Test Explorer
- **通用**: GitLens, YAML, Markdown All in One

### GoLand / IntelliJ IDEA

- 启用 `go vet` 和 `go test` 自动运行
- 配置 Maven 自动导入
- 启用 TypeScript/JavaScript 支持

## 下一步

- 阅读 [PR-FLOW.md](../PR-FLOW.md) 了解提交流程
- 阅读 [engineering-rules.md](../02-governance/engineering-rules.md) 了解工程规范
- 查看 [API 文档](../03-api/) 了解接口定义

---

_最后更新：2026-03-02_

## 本地验证脚本

### 完整检查脚本

`scripts/local-test.sh` 提供完整的本地验证流程，支持分阶段运行：

```bash
# 运行所有检查（默认）
./scripts/local-test.sh

# 或指定检查项
./scripts/local-test.sh --frontend    # 仅前端检查
./scripts/local-test.sh --server      # 仅服务端检查
./scripts/local-test.sh --agent       # 仅 Agent 检查
./scripts/local-test.sh --lint        # 仅代码风格检查
./scripts/local-test.sh --all         # 所有检查

# 查看帮助
./scripts/local-test.sh --help
```

**使用场景**：
- 提交 PR 前的完整验证
- 开发过程中阶段性检查
- CI 失败后的本地复现

### 快速检查脚本

`scripts/quick-check.sh` 提供快速验证（1 分钟内完成）：

```bash
# 运行快速检查
./scripts/quick-check.sh
```

**检查内容**：
- 修改的 Go 文件语法检查（`go vet`）
- 修改的 TypeScript 文件格式检查（Prettier）
- 合并标记检测

**使用场景**：
- commit 前快速验证
- 开发过程中频繁检查

### Git Pre-commit Hook

项目提供 pre-commit hook 自动运行快速检查：

```bash
# 安装 hook（二选一）

# 方法 1：手动复制
cp .githooks/pre-commit .git/hooks/pre-commit

# 方法 2：使用 Git 配置（推荐）
git config core.hooksPath .githooks
```

安装后，每次 commit 会自动运行 `quick-check.sh`。

**跳过检查**（不推荐）：
```bash
git commit --no-verify -m "your message"
```

详细说明见 `.githooks/README.md`。

## 推荐工作流程

### 日常开发

```bash
# 1. 开发过程中频繁运行快速检查
./scripts/quick-check.sh

# 2. 完成功能后运行完整检查
./scripts/local-test.sh --all

# 3. 提交代码（pre-commit hook 自动验证）
git add .
git commit -m "feat: your feature"

# 4. 推送前再次确认
./scripts/local-test.sh --all
git push
```

### PR 提交流程

```bash
# 1. 从 main 拉分支
git checkout -b feat/your-feature main

# 2. 开发完成后运行完整检查
./scripts/local-test.sh --all

# 3. 提交并推送
git commit -m "feat: your feature"
git push origin feat/your-feature

# 4. 创建 PR，等待 CI 验证
```

详见 [PR-FLOW.md](PR-FLOW.md)。
