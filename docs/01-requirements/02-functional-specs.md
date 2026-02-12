# 02 详细功能规格 (Functional Specifications)

## 1. 基础设施与 Agent 实例管理 (Infrastructure & Instance)

### 1.1 环境管理 (Environment Management)
*   **核心功能**：环境的增删改查（CRUD）。
*   **环境隔离**：不同环境下的主机与 Agent 物理或逻辑隔离。

### 1.2 主机管理 (Host Management)
*   **纳管主机 (Host Onboarding)**：
    *   **步骤 1**: 运维人员在界面创建主机记录（IP、名称、网关地址）。
    *   **步骤 2**: 系统生成 **Host Agent 安装包**（包含二进制与配置文件），配置文件中指定 `listen_port` 与 `secret_key`。
    *   **步骤 3**: 运维人员在目标主机上手动部署并启动 Host Agent（监听本地端口）。
    *   **步骤 4**: 运维人员在界面点击“连接”或 Server 自动轮询，Server 主动连接 Host Agent 网关地址。连接成功后，将主机状态标记为“在线”。
*   **主机详情页**：
    *   展示主机基础信息与 Host Agent 连通状态。
    *   **核心功能区域**：展示该主机下已部署的 **Agent 实例列表**。
*   **主机级 Agent 管理**：
    *   **新增 Agent 实例**：
        *   入口：在主机详情页点击“添加 Agent”。
        *   操作：选择一个 **Agent 模板**（AgentTemplate）。
        *   逻辑：系统基于模板在该主机下生成一个 Agent 实例（AgentInstance）。
    *   **移除 Agent 实例**：从主机上卸载/删除 Agent。

### 1.3 终端与命令管理 (Terminal & Command Management)
*   **Web 终端 (Web Console)**：
    *   **交互式 Shell**：提供基于 WebSocket 的实时终端交互，支持标准输入输出。
    *   **日志回溯**：连接建立时，自动拉取并展示 Host Agent 最近 100 行日志。
    *   **状态同步**：实时展示 Host Agent 的心跳状态。
*   **命令管理 (Command Management)**：
    *   **命令库**：系统级维护常用终端命令（TerminalCommand），包含名称、脚本、适用系统（Linux/Windows/ALL）及描述。
    *   **命令面板 (Command Palette)**：在终端界面提供快捷入口，允许用户从命令库中选择并一键执行命令。
    *   **执行逻辑**：支持发送自定义脚本或预设命令至 Host Agent 执行，Agent 自动识别操作系统调用对应 Shell（cmd/sh）。

## 2. Agent 模板与资源定义 (Template & Resources)

### 2.1 Agent 模板管理
*   **定义模板**：
    *   基础信息：名称、描述、默认配置。
    *   **资源来源配置**：关联代码/包的获取方式（Git/Docker/HTTP等）。
    *   **命令定义 (Command Definitions)**：
        *   在模板层面预定义可执行的运维命令（如 `start`, `stop`, `backup`, `check_status`）。
        *   定义命令的脚本内容、默认参数、超时时间。
        *   **意义**：将运维操作标准化，避免临时手敲命令的风险。

### 2.2 资源来源 (Resource Source)
*   **类型支持**：本地上传、Git 仓库、HTTP 下载、Docker 仓库、阿里云制品库。
*   **版本控制**：资源配置的变更应有版本记录。

## 3. Agent 运行与运维 (Operations)

### 3.1 状态流转 (Lifecycle)
*   **Host Agent 职责**：作为所有部署动作的执行者。
*   **流程**：
    1.  **准备**：Host Agent 根据模板配置，下载资源包（从 Server 或直接从外部源）。
    2.  **打包**：(可选) 若需本地编译，Host Agent 执行构建脚本。
    3.  **部署**：Host Agent 将制品解压/安装到指定目录。
    4.  **运行**：Host Agent 启动目标进程。
*   此流程在“主机 -> Agent 实例”的上下文中触发与展示。

### 3.2 命令执行 (Command Execution)
*   **操作入口**：在“主机详情 -> Agent 实例”的操作栏中。
*   **调度中心**：
    *   Server 将指令下发给 **Host Agent**。
    *   Host Agent 根据指令中的 target 标识，调度具体的 Agent 或直接执行 Shell 脚本操作目标 Agent 实例。
*   **执行逻辑**：
    *   用户从下拉列表中选择该 Agent 模板预定义的**命令**。
    *   （可选）用户覆盖默认参数。
    *   系统下发指令至 Host Agent。

### 3.3 Agent (插件/功能)
*   **定位**：Agent 为轻量级的、无状态的功能单元或脚本（如 Nginx 管理、文件传输工具、数据库备份脚本）。
*   **分发**：Agent 程序本身由 Host Agent 根据模板定义下载并管理。
*   **调用执行**：Host Agent 接收到 Server 指令时，调起对应 Agent 执行特定任务，并收集其退出码与输出。

## 4. 观测与审计
*   **日志**：Agent 运行日志、命令执行输出日志。
*   **审计**：记录所有对主机、Agent 的变更及命令执行记录。
