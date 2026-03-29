#!/bin/bash
cd /home/esa-runner/es-agents
echo "=== OpenAPI 注解状态检查 ==="
for f in server/src/main/java/com/easystation/deployment/resource/*.java; do
    name=$(basename "$f")
    count=$(grep -c "@Operation" "$f" 2>/dev/null || echo "0")
    methods=$(grep -cE "^\s+@(GET|POST|PUT|DELETE)" "$f" 2>/dev/null || echo "0")
    echo "$name: $count/@Operation / $methods 方法"
done
echo ""
echo "=== HostResource ==="
hcount=$(grep -c "@Operation" server/src/main/java/com/easystation/infra/resource/HostResource.java 2>/dev/null || echo "0")
hmethods=$(grep -cE "^\s+@(GET|POST|PUT|DELETE)" server/src/main/java/com/easystation/infra/resource/HostResource.java 2>/dev/null || echo "0")
echo "HostResource.java: $hcount/@Operation / $hmethods 方法"
