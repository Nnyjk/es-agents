#!/bin/bash
# ESA 演示环境启动脚本
# 用法：./scripts/start-demo-env.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "========================================"
echo "ESA 演示环境启动"
echo "========================================"
echo ""

# 检查 Server
echo "[1/3] 检查 Server..."
if [ -f "$PROJECT_ROOT/server/target/quarkus-app/quarkus-run.jar" ]; then
    echo "  ✅ Server 已编译"
else
    echo "  ⚠️  Server 未编译，开始编译..."
    cd "$PROJECT_ROOT/server"
    ./mvnw clean package -DskipTests -q
    echo "  ✅ Server 编译完成"
fi

# 检查 Frontend
echo "[2/3] 检查 Frontend..."
if [ -d "$PROJECT_ROOT/frontend/node_modules" ]; then
    echo "  ✅ Frontend 依赖已安装"
else
    echo "  ⚠️  Frontend 依赖未安装，开始安装..."
    cd "$PROJECT_ROOT/frontend"
    npm install
    echo "  ✅ Frontend 依赖安装完成"
fi

# 检查 Agent
echo "[3/3] 检查 Agent..."
if [ -f "$PROJECT_ROOT/agent/esa-agent" ]; then
    echo "  ✅ Agent 已编译"
else
    echo "  ⚠️  Agent 未编译，开始编译..."
    cd "$PROJECT_ROOT/agent"
    go build -o esa-agent ./cmd/host-agent/...
    echo "  ✅ Agent 编译完成"
fi

echo ""
echo "========================================"
echo "✅ 所有组件准备就绪"
echo "========================================"
echo ""
echo "启动命令:"
echo "  Server:   cd server && java -jar target/quarkus-app/quarkus-run.jar"
echo "  Frontend: cd frontend && npm run dev"
echo "  Agent:    cd agent && ./esa-agent --server-url http://localhost:8080"
echo ""
echo "访问地址:"
echo "  Frontend: http://localhost:3000"
echo "  Server:   http://localhost:8080"
echo ""
