# ES Agents 快速入门指南

**5 分钟快速上手 ES Agents 平台**

---

## 前置条件

- Java 17+（Server 端）
- Go 1.21+ 或预编译 Agent 二进制
- PostgreSQL 14+
- Docker（可选，用于监控栈部署）

---

## 步骤 1：启动 Server

```bash
# 克隆仓库
git clone https://github.com/Nnyjk/es-agents.git
cd es-agents/server

# 配置数据库（复制示例配置并修改）
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties

# 构建并启动
./mvnw clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

Server 启动后访问：http://localhost:8080

---

## 步骤 2：部署 Agent

```bash
# 编译 Agent
cd ../agent
go build -o esa-agent

# 配置 Agent
cp config.yaml.example config.yaml
# 编辑 config.yaml，设置 Server 地址和 Token

# 启动 Agent
./esa-agent
```

---

## 步骤 3：添加主机

1. 访问 http://localhost:8080 登录平台
2. 进入「主机管理」→「添加主机」
3. 填写主机信息：
   - 名称：`local-host`
   - IP：`127.0.0.1`
   - SSH 端口：`22`
   - 认证方式：选择密钥或密码
4. 点击「测试连接」验证
5. 点击「保存」

---

## 步骤 4：执行第一个命令

1. 进入「命令执行」页面
2. 选择刚添加的主机
3. 输入命令：`hostname`
4. 点击「执行」
5. 查看执行结果

---

## 步骤 5：部署监控（可选）

```bash
# 启动 Prometheus + Grafana
cd monitoring
docker-compose up -d
```

访问监控面板：
- Grafana: http://localhost:3000 (admin/esa-admin)
- Prometheus: http://localhost:9090

---

## 下一步

- 📖 阅读 [用户使用手册](./USER-MANUAL.md) 了解完整功能
- 🔧 配置告警规则，接收系统通知
- 🚀 创建第一个部署任务
- 📊 查看 Grafana 监控面板

---

**遇到问题？** 查看 [常见问题解答](./USER-MANUAL.md#第四章常见问题解答)
