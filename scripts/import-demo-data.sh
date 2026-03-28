#!/bin/bash
# ESA M1 验收演示数据导入脚本
# 用法：./scripts/import-demo-data.sh

set -e

BASE_URL="http://localhost:8080"

echo "========================================"
echo "ESA M1 验收演示数据导入"
echo "========================================"
echo ""

# 1. 创建演示环境
echo "[1/5] 创建演示环境..."
ENV_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/environments" \
  -H "Content-Type: application/json" \
  -d '{"code":"demo","name":"Demo Environment","description":"M1 验收演示环境"}')
ENV_ID=$(echo "$ENV_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "  ✅ Environment ID: $ENV_ID"

# 2. 创建测试主机
echo "[2/5] 创建测试主机..."
HOSTS=(
  "demo-host-01:192.168.1.101:Linux:Ubuntu 22.04"
  "demo-host-02:192.168.1.102:Linux:CentOS 7.9"
  "demo-host-03:192.168.1.103:Linux:Debian 11"
  "demo-host-04:192.168.1.104:Windows:Windows Server 2019"
  "demo-host-05:192.168.1.105:Linux:Ubuntu 20.04"
)

for host_info in "${HOSTS[@]}"; do
  IFS=':' read -r hostname ip os_type os_version <<< "$host_info"
  curl -s -X POST "$BASE_URL/api/v1/hosts" \
    -H "Content-Type: application/json" \
    -d "{
      \"environmentId\":\"$ENV_ID\",
      \"hostname\":\"$hostname\",
      \"name\":\"${hostname^}\",
      \"ip\":\"$ip\",
      \"osType\":\"${os_type,,}\",
      \"osVersion\":\"$os_version\",
      \"status\":\"ONLINE\"
    }" > /dev/null
  echo "  ✅ Host: $hostname ($ip)"
done

# 3. 创建命令模板
echo "[3/5] 创建命令模板..."
TEMPLATES=(
  "check-disk:Check Disk Usage:df -h"
  "check-memory:Check Memory Usage:free -m"
  "check-cpu:Check CPU Usage:top -bn1 | head -20"
  "restart-nginx:Restart Nginx:sudo systemctl restart nginx"
  "deploy-app:Deploy Application:./deploy.sh"
  "backup-db:Backup Database:pg_dump -U postgres esa > backup.sql"
  "check-logs:Check System Logs:journalctl -xe --no-pager | tail -50"
  "update-packages:Update Packages:sudo apt update && sudo apt upgrade -y"
)

for template_info in "${TEMPLATES[@]}"; do
  IFS=':' read -r code name script <<< "$template_info"
  curl -s -X POST "$BASE_URL/commands/templates" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\":\"$name\",
      \"description\":\"Demo command template: $code\",
      \"script\":\"$script\",
      \"category\":\"SYSTEM\",
      \"timeout\":300
    }" > /dev/null
  echo "  ✅ Template: $code"
done

# 4. 创建系统事件日志
echo "[4/5] 创建系统事件日志..."
EVENTS=(
  "USER_LOGIN:INFO:Demo user logged in"
  "HOST_ADDED:INFO:Demo environment initialized with 5 hosts"
  "DEPLOYMENT_STARTED:INFO:Deployment pipeline triggered"
  "DEPLOYMENT_SUCCESS:SUCCESS:All hosts deployed successfully"
  "COMMAND_EXECUTED:INFO:Health check commands executed"
)

for event_info in "${EVENTS[@]}"; do
  IFS=':' read -r event_type level message <<< "$event_info"
  curl -s -X POST "$BASE_URL/v1/system-event-logs" \
    -H "Content-Type: application/json" \
    -d "{
      \"eventType\":\"$event_type\",
      \"eventLevel\":\"$level\",
      \"message\":\"$message\"
    }" > /dev/null
  echo "  ✅ Event: $event_type"
done

echo ""
echo "========================================"
echo "✅ 演示数据导入完成!"
echo "========================================"
echo ""
echo "验证:"
echo "  - 环境：curl $BASE_URL/api/v1/environments"
echo "  - 主机：curl $BASE_URL/api/v1/hosts"
echo "  - 命令模板：curl $BASE_URL/commands/templates"
echo "  - 事件日志：curl $BASE_URL/v1/system-event-logs"
