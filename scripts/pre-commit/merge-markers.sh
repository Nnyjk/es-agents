#!/usr/bin/env bash
set -e

echo "=== 合并冲突标记检查 ==="

# 检查合并冲突标记
if grep -r -nE --exclude-dir=.git --exclude-dir=node_modules \
  "^(<<<<<<<|=======|>>>>>>>)" . 2>/dev/null; then
  echo ""
  echo "❌ 发现未解决的合并冲突标记！"
  echo "请手动解决冲突后再提交。"
  exit 1
fi

echo "✅ 无合并冲突标记"