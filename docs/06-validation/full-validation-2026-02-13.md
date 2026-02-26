# 全量验证报告（2026-02-13）

## 1. 编译/构建验证

### Server（Quarkus / Maven）
- 命令：`mvn -f server/pom.xml clean package -DskipTests`
- 结果：`BUILD SUCCESS`
- 产物：`server/target/easy-station-server-1.0.0-SNAPSHOT.jar`

### Frontend（React / Vite）
- 首次执行 `npm ci` 因 `xterm-for-react@1.0.4` 与 React 18 的 peer 依赖约束冲突失败。
- 采用 `npm ci --legacy-peer-deps` 后执行 `npm run build` 成功。
- 结果：Vite 生产构建成功，输出到 `frontend/dist/`。

### Agent（Go）
- 命令：`go test ./...`（当前仓库 agent 包无测试文件，编译检查通过）
- 命令：
  - `go build -o dist/host-agent ./cmd/host-agent`
  - `go build -o dist/tool-exec ./cmd/tool-exec`
- 结果：Agent 可执行产物成功生成。

## 2. 前后端接口对接验证

### 验证方式
- 脚本：`scripts/validate_api_contract.py`
- 核心逻辑：
  1. 扫描 `frontend/src/services/*.ts` 中 `request.get/post/put/delete` 调用。
  2. 扫描 `server/src/main/java/com/easystation/**/resource/*.java` 中 JAX-RS 注解路由。
  3. 将前端 `${id}` 与后端 `{id}` 统一归一化后按 `method + path` 比对。

### 脚本改进（根据 review 调整）
- 移除硬编码仓库路径，默认自动以脚本所在仓库根目录为准，支持 `--repo` 参数覆盖。
- 增加 `--strict` 参数，在存在缺失映射时返回非零退出码，便于 CI 集成。

### 验证结论
- 本次执行结果：
  - Frontend endpoints: **26**
  - Backend endpoints: **65**
  - Missing mappings: **0**

结论：当前前端服务层调用接口在后端资源层均可匹配到对应路由（静态契约层面通过）。

## 3. Agent 产物产出验证

通过本次验证生成 Agent 二进制产物：

- `agent/dist/host-agent`（约 15MB）
  - SHA256: `9197a06bf87b2a95705be729da61ad1496beebdee14e409d54ac5b7222c8ca0c`
- `agent/dist/tool-exec`（约 5.8MB）
  - SHA256: `ce352b7f8d57d5ae535aee96981cea23d2863c1544f09945b5217191c2f6240b`

> 说明：二进制产物为验证阶段临时生成，不纳入版本库提交。
