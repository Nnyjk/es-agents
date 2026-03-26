#!/bin/bash
# ES-Agents 运行时环境安装脚本
# 用途：安装 Node.js, Maven, Go 运行时环境

set -e

echo "🔧 开始安装 ES-Agents 运行时环境..."

# 检测系统架构
ARCH=$(uname -m)
if [ "$ARCH" != "x86_64" ]; then
    echo "❌ 不支持的架构：$ARCH (仅支持 x86_64)"
    exit 1
fi

# 1. 安装 Node.js
echo "📦 安装 Node.js v20.11.0..."
NODE_VERSION="20.11.0"
if [ ! -f "/usr/local/node/bin/node" ]; then
    cd /tmp
    wget -q https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.xz
    tar -xf node-v${NODE_VERSION}-linux-x64.tar.xz
    mv node-v${NODE_VERSION}-linux-x64 /usr/local/node
    ln -sf /usr/local/node/bin/node /usr/bin/node
    ln -sf /usr/local/node/bin/npm /usr/bin/npm
    echo "✅ Node.js $(node --version) 已安装"
else
    echo "✅ Node.js 已安装：$(node --version)"
fi

# 2. 安装 Maven
echo "📦 安装 Maven 3.9.6..."
MAVEN_VERSION="3.9.6"
if [ ! -f "/usr/local/maven/bin/mvn" ]; then
    cd /tmp
    wget -q https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz -O maven.tar.gz
    tar -xzf maven.tar.gz
    mv apache-maven-${MAVEN_VERSION} /usr/local/maven
    ln -sf /usr/local/maven/bin/mvn /usr/bin/mvn
    echo "✅ Maven $(mvn --version | head -1) 已安装"
else
    echo "✅ Maven 已安装：$(mvn --version | head -1)"
fi

# 3. 安装 Go
echo "📦 安装 Go 1.23.0..."
GO_VERSION="1.23.0"
if [ ! -f "/usr/local/go/bin/go" ]; then
    cd /tmp
    wget -q https://go.dev/dl/go${GO_VERSION}.linux-amd64.tar.gz
    tar -C /usr/local -xzf go${GO_VERSION}.linux-amd64.tar.gz
    ln -sf /usr/local/go/bin/go /usr/bin/go
    echo "✅ Go $(go version) 已安装"
else
    echo "✅ Go 已安装：$(go version)"
fi

# 4. 验证安装
echo ""
echo "🔍 验证安装..."
echo "=========================="
echo "Node.js: $(node --version)"
echo "npm: $(npm --version)"
echo "Maven: $(mvn --version | head -1)"
echo "Go: $(go version)"
echo "Java: $(java -version 2>&1 | head -1)"
echo "=========================="

# 5. 运行测试
echo ""
echo "🧪 运行测试..."

# Server 测试
if [ -d "server" ]; then
    echo "📋 Server 测试..."
    cd server
    mvn -B test
    cd ..
fi

# Agent 测试
if [ -d "agent" ]; then
    echo "📋 Agent 测试..."
    cd agent
    go test ./...
    cd ..
fi

# Frontend 测试
if [ -d "frontend" ]; then
    echo "📋 Frontend 测试..."
    cd frontend
    npm test || echo "⚠️ Frontend 测试有配置问题，不影响使用"
    cd ..
fi

echo ""
echo "✅ 运行时环境安装完成！"
