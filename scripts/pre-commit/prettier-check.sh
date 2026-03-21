#!/usr/bin/env bash
set -e

echo "=== Prettier 格式检查与自动修复 ==="

# 自动修复格式问题
npx --yes prettier@3.5.3 --write \
  ".github/**/*.{yml,yaml,md}" \
  "docs/**/*.md" \
  "frontend/src/**/*.{ts,tsx}" \
  2>/dev/null

# 检查是否有文件被修改
CHANGED=$(git diff --name-only)
if [ -n "$CHANGED" ]; then
  echo ""
  echo "⚠️  发现格式问题已自动修复，请重新 stage 并提交："
  echo "$CHANGED"
  echo ""
  echo "运行: git add -A && git commit --amend --no-edit"
  exit 1
fi

echo "✅ 格式检查通过"